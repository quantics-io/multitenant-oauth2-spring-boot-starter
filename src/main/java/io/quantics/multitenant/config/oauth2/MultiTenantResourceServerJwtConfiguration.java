package io.quantics.multitenant.config.oauth2;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.quantics.multitenant.tenant.model.Tenant;
import io.quantics.multitenant.tenant.service.TenantService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@EnableConfigurationProperties(MultiTenantResourceServerProperties.class)
@ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "enabled",
        havingValue = "true")
public class MultiTenantResourceServerJwtConfiguration {

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

}
