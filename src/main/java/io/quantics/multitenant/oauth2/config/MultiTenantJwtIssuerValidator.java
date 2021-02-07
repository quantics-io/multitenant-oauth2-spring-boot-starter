package io.quantics.multitenant.oauth2.config;

import io.quantics.multitenant.tenantdetails.TenantDetails;
import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of a {@link OAuth2TokenValidator} that validates the <i>issuer</i> of a {@link Jwt} against a
 * whitelist of allowed tenants. The allowed tenants are provided by a {@link TenantDetailsService}.
 *
 * @see OAuth2TokenValidator
 */
public class MultiTenantJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private final TenantDetailsService tenantService;
    private final Map<String, JwtIssuerValidator> validators = new ConcurrentHashMap<>();

    public MultiTenantJwtIssuerValidator(TenantDetailsService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        return this.validators.computeIfAbsent(toTenant(token), this::fromTenant)
                .validate(token);
    }

    private String toTenant(Jwt jwt) {
        return jwt.getIssuer().toString();
    }

    private JwtIssuerValidator fromTenant(String issuer) {
        return this.tenantService.getByIssuer(issuer)
                .map(TenantDetails::getIssuer)
                .map(JwtIssuerValidator::new)
                .orElseThrow(() -> new IllegalArgumentException("Unknown tenant"));
    }

}
