package io.quantics.multitenant.config.web.oauth2;

import io.quantics.multitenant.tenant.model.Tenant;
import io.quantics.multitenant.tenant.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiTenantJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private final TenantService tenantService;
    private final Map<String, JwtIssuerValidator> validators = new ConcurrentHashMap<>();

    @Autowired
    public MultiTenantJwtIssuerValidator(TenantService tenantService) {
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

    private JwtIssuerValidator fromTenant(String tenant) {
        return this.tenantService.getByIssuer(tenant)
                .map(Tenant::getIssuer)
                .map(JwtIssuerValidator::new)
                .orElseThrow(() -> new IllegalArgumentException("unknown tenant"));
    }
}
