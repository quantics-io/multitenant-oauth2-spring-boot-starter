package io.quantics.multitenant.config.web.oauth2;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.quantics.multitenant.config.web.oauth2.KeycloakRealmAuthoritiesConverter;
import io.quantics.multitenant.config.web.oauth2.MultiTenantJWSKeySelector;
import io.quantics.multitenant.config.web.oauth2.MultiTenantJwtIssuerValidator;
import io.quantics.multitenant.tenant.model.Tenant;
import io.quantics.multitenant.tenant.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class MultiTenantResourceServerJwtConfiguration {

    @Configuration
    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "enabled",
            havingValue = "true")
    static class JwtConfiguration {

        @Bean
        @ConditionalOnMissingBean(JWTProcessor.class)
        public JWTProcessor<SecurityContext> multiTenantJwtProcessor(MultiTenantJWSKeySelector keySelector) {
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(keySelector);
            return jwtProcessor;
        }

        @Bean
        @ConditionalOnMissingBean(JwtDecoder.class)
        public JwtDecoder multiTenantJwtDecoder(JWTProcessor<SecurityContext> multiTenantJwtProcessor,
                                                MultiTenantJwtIssuerValidator jwtValidator) {
            NimbusJwtDecoder decoder = new NimbusJwtDecoder(multiTenantJwtProcessor);
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), jwtValidator));
            return decoder;
        }

    }


    @Configuration
    @ConditionalOnMissingBean(AuthenticationManagerResolver.class)
    static class MultiTenantAuthenticationManagerResolver {

        @Bean
        @ConditionalOnBean(JwtDecoder.class)
        public AuthenticationManagerResolver<HttpServletRequest> multiTenantAuthenticationManagerResolver() {
            return new AuthenticationManagerResolver<>() {

                private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();
                private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();
                @Autowired
                private TenantService tenantService;
                @Autowired
                private JwtDecoder multiTenantJwtDecoder;

                @Override
                public AuthenticationManager resolve(HttpServletRequest request) {
                    return this.authenticationManagers.computeIfAbsent(toTenant(request), this::fromTenant);
                }

                private String toTenant(HttpServletRequest request) {
                    try {
                        String token = this.resolver.resolve(request);
                        return JWTParser.parse(token).getJWTClaimsSet().getIssuer();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }

                private AuthenticationManager fromTenant(String tenant) {
                    return this.tenantService.getByIssuer(tenant)
                            .map(Tenant::getIssuer)
                            .map(i -> multiTenantJwtDecoder)
                            .map(d -> {
                                JwtAuthenticationProvider provider = new JwtAuthenticationProvider(d);
                                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                                converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmAuthoritiesConverter());
                                provider.setJwtAuthenticationConverter(converter);
                                return provider;
                            })
                            .orElseThrow(() -> new IllegalArgumentException("Unknown tenant"))::authenticate;
                }
            };
        }

    }


    @Configuration
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    static class MultiTenantWebSecurityConfigurerAdapter {

        @Bean
        @ConditionalOnBean(JwtDecoder.class)
        WebSecurityConfigurerAdapter authenticationManagerResolverWebSecurityConfigurerAdapter() {
            return new WebSecurityConfigurerAdapter() {

                @Autowired
                private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

                @Override
                protected void configure(HttpSecurity http) throws Exception {
                    http.authorizeRequests(requests -> requests
                            .anyRequest().authenticated());
                    http.oauth2ResourceServer(resourceServer -> resourceServer
                            .authenticationManagerResolver(this.authenticationManagerResolver));
                }

            };
        }

    }

}
