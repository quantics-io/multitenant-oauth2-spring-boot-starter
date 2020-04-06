package io.quantics.multitenant.config.oauth2;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Configures a {@link JwtDecoder} and exposes it as a bean.
 * The {@link JwtDecoder} uses a {@link JWTProcessor} with a {@link MultiTenantJWSKeySelector} and a
 * {@link MultiTenantJwtIssuerValidator}.
 */
@Configuration
@Conditional(JwtCondition.class)
public class MultiTenantResourceServerJwtConfiguration {

    @Bean
    @ConditionalOnMissingBean(JWTClaimsSetAwareJWSKeySelector.class)
    JWTClaimsSetAwareJWSKeySelector<SecurityContext> multiTenantJWSKeySelector(TenantDetailsService tenantService) {
        return new MultiTenantJWSKeySelector(tenantService);
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
    OAuth2TokenValidator<Jwt> multiTenantJwtIssuerValidator(TenantDetailsService tenantService) {
        return new MultiTenantJwtIssuerValidator(tenantService);
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

}
