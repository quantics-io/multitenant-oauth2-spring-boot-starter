package io.quantics.multitenant.config.oauth2;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
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
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class MultiTenantResourceServerJwtConfiguration {

    @Configuration
    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "enabled",
            havingValue = "true")
    static class JwtConfiguration {

        @Bean
        @ConditionalOnMissingBean(JWTClaimsSetAwareJWSKeySelector.class)
        JWTClaimsSetAwareJWSKeySelector<SecurityContext> multiTenantJWSKeySelector() {
            return new JWTClaimsSetAwareJWSKeySelector<>() {

                @Autowired
                private TenantService tenantService;
                private final Map<String, JWSKeySelector<SecurityContext>> selectors = new ConcurrentHashMap<>();

                @Override
                public List<? extends Key> selectKeys(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet,
                                                      SecurityContext securityContext) throws KeySourceException {
                    return this.selectors.computeIfAbsent(toTenant(jwtClaimsSet), this::fromTenant)
                            .selectJWSKeys(jwsHeader, securityContext);
                }

                private String toTenant(JWTClaimsSet claimSet) {
                    return claimSet.getIssuer();
                }

                private JWSKeySelector<SecurityContext> fromTenant(String tenant) {
                    return this.tenantService.getByIssuer(tenant)
                            .map(Tenant::getJwkSetUrl)
                            .map(this::fromUri)
                            .orElseThrow(() -> new IllegalArgumentException("Unknown tenant"));
                }

                private JWSKeySelector<SecurityContext> fromUri(String uri) {
                    try {
                        return JWSAlgorithmFamilyJWSKeySelector.fromJWKSetURL(new URL(uri));
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            };
        }

        @Bean
        @ConditionalOnMissingBean(OAuth2TokenValidator.class)
        OAuth2TokenValidator<Jwt> multiTenantJwtIssuerValidator() {
            return new OAuth2TokenValidator<>() {

                @Autowired
                private TenantService tenantService;
                private final Map<String, JwtIssuerValidator> validators = new ConcurrentHashMap<>();

                @Override
                public OAuth2TokenValidatorResult validate(Jwt token) {
                    return this.validators.computeIfAbsent(toTenant(token), this::fromTenant)
                            .validate(token);
                }

                private String toTenant(Jwt jwt) {
                    return jwt.getIssuer().toString();
                }

                private JwtIssuerValidator fromTenant(String tenant) {
                    return this.tenantService.getByIssuer(tenant)
                            .map(Tenant::getIssuer)
                            .map(JwtIssuerValidator::new)
                            .orElseThrow(() -> new IllegalArgumentException("unknown tenant"));
                }
            };
        }

        @Bean
        @ConditionalOnMissingBean(JWTProcessor.class)
        public JWTProcessor<SecurityContext> multiTenantJwtProcessor(
                JWTClaimsSetAwareJWSKeySelector<SecurityContext> multiTenantJWSKeySelector) {
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(multiTenantJWSKeySelector);
            return jwtProcessor;
        }

        @Bean
        @ConditionalOnMissingBean(JwtDecoder.class)
        public JwtDecoder multiTenantJwtDecoder(JWTProcessor<SecurityContext> multiTenantJwtProcessor,
                                                OAuth2TokenValidator<Jwt> multiTenantJwtIssuerValidator) {
            NimbusJwtDecoder decoder = new NimbusJwtDecoder(multiTenantJwtProcessor);
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(),
                    multiTenantJwtIssuerValidator));
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
