package io.quantics.multitenant.config.oauth2;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.quantics.multitenant.tenant.model.Tenant;
import io.quantics.multitenant.tenant.service.TenantService;
import io.quantics.multitenant.util.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableConfigurationProperties(MultiTenantResourceServerProperties.class)
public class MultiTenantResourceServerJwtConfiguration {

    @Configuration
    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "enabled",
            havingValue = "true")
    static class JwtConfiguration {

        @Bean
        @ConditionalOnMissingBean(JWTClaimsSetAwareJWSKeySelector.class)
        JWTClaimsSetAwareJWSKeySelector<SecurityContext> multiTenantJWSKeySelector(TenantService tenantService) {
            return new MultiTenantJWSKeySelector(iss -> tenantService.getByIssuer(iss).map(Tenant::getJwkSetUrl));
        }

        @Bean
        @ConditionalOnMissingBean(JWTProcessor.class)
        JWTProcessor<SecurityContext> multiTenantJwtProcessor(
                JWTClaimsSetAwareJWSKeySelector<SecurityContext> multiTenantJWSKeySelector) {
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(multiTenantJWSKeySelector);
            return jwtProcessor;
        }

        @Bean
        @ConditionalOnMissingBean(OAuth2TokenValidator.class)
        OAuth2TokenValidator<Jwt> multiTenantJwtIssuerValidator(TenantService tenantService) {
            return new MultiTenantJwtIssuerValidator(iss -> tenantService.getByIssuer(iss).map(Tenant::getIssuer));
        }

        @Bean
        @ConditionalOnMissingBean(JwtDecoder.class)
        JwtDecoder multiTenantJwtDecoder(JWTProcessor<SecurityContext> multiTenantJwtProcessor,
                                         OAuth2TokenValidator<Jwt> multiTenantJwtIssuerValidator) {
            NimbusJwtDecoder decoder = new NimbusJwtDecoder(multiTenantJwtProcessor);
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(),
                    multiTenantJwtIssuerValidator));
            return decoder;
        }

        @Bean
        @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
        WebSecurityConfigurerAdapter multiTenantWebSecurityConfigurerAdapter(JwtDecoder multiTenantJwtDecoder) {
            return new WebSecurityConfigurerAdapter() {

                @Override
                protected void configure(HttpSecurity http) throws Exception {
                    http.authorizeRequests(authorize -> authorize
                            .anyRequest().authenticated());
                    http.oauth2ResourceServer(oauth2 -> oauth2
                            .jwt(jwt -> jwt
                                    .decoder(multiTenantJwtDecoder)));
                }
            };
        }

    }

    @Configuration
    @ConditionalOnMissingBean(WebMvcConfigurer.class)
    @Slf4j
    static class MultiTenantWebMvcConfiguration {

        @Bean
        @ConditionalOnProperty(value = {
                "spring.security.oauth2.resourceserver.multitenant.enabled",
                "spring.security.oauth2.resourceserver.multitenant.use-header"
        }, havingValue = "true")
        HandlerInterceptorAdapter multiTenantHeaderInterceptor() {
            return new HandlerInterceptorAdapter() {

                @Override
                public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                    String tenantId = request.getHeader("X-TENANT-ID");
                    log.debug("Set TenantContext: {}", tenantId);
                    TenantContext.setTenantId(tenantId);
                    return true;
                }

                @Override
                public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                                       ModelAndView modelAndView) {
                    log.debug("Clear TenantContext: {}", TenantContext.getTenantId());
                    TenantContext.clear();
                }

            };
        }

        @Bean
        @ConditionalOnProperty(value = {
                "spring.security.oauth2.resourceserver.multitenant.enabled",
                "spring.security.oauth2.resourceserver.multitenant.use-token"
        }, havingValue = "true")
        HandlerInterceptorAdapter multiTenantJwtInterceptor() {
            return new HandlerInterceptorAdapter() {

                @Autowired
                private TenantService tenantService;

                @Override
                @SuppressWarnings("rawtypes")
                public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication instanceof AbstractOAuth2TokenAuthenticationToken) {
                        AbstractOAuth2TokenAuthenticationToken token = (AbstractOAuth2TokenAuthenticationToken) authentication;
                        String issuer = (String) token.getTokenAttributes().get("iss");
                        Tenant tenant = this.tenantService.getByIssuer(issuer)
                                .orElseThrow(() -> new IllegalArgumentException("Tenant not found for issuer: " + issuer));
                        log.debug("Set TenantContext: {}", tenant.getId());
                        TenantContext.setTenantId(tenant.getId());
                    }
                    return true;
                }

                @Override
                public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                                       ModelAndView modelAndView) {
                    if (TenantContext.getTenantId() != null) {
                        log.debug("Clear TenantContext: {}", TenantContext.getTenantId());
                        TenantContext.clear();
                    }
                }
            };
        }

        @Bean
        @ConditionalOnBean(HandlerInterceptorAdapter.class)
        WebMvcConfigurer multiTenantWebMvcConfigurer() {
            return new WebMvcConfigurer() {

                @Autowired
                private HandlerInterceptorAdapter interceptor;

                @Override
                public void addInterceptors(InterceptorRegistry registry) {
                    registry.addInterceptor(interceptor);
                }
            };
        }

    }

}
