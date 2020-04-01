package io.quantics.multitenant.config.oauth2;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MultiTenantJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private final Function<String, Optional<String>> jwtIssuerToTenantIssuer;
    private final Map<String, JwtIssuerValidator> validators = new ConcurrentHashMap<>();

    public MultiTenantJwtIssuerValidator(Function<String, Optional<String>> jwtIssuerToTenantIssuer) {
        this.jwtIssuerToTenantIssuer = jwtIssuerToTenantIssuer;
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
        return this.jwtIssuerToTenantIssuer.apply(issuer)
                .map(JwtIssuerValidator::new)
                .orElseThrow(() -> new IllegalArgumentException("unknown tenant"));
    }

}
