package io.quantics.multitenant.config.web.oauth2;

import com.nimbusds.jwt.JWTParser;
import io.quantics.multitenant.tenant.model.Tenant;
import io.quantics.multitenant.tenant.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiTenantAuthenticationManager implements AuthenticationManagerResolver<HttpServletRequest> {

    private final TenantService tenantService;
    private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();
    private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();
    private final JwtDecoder multiTenantJwtDecoder;

    @Autowired
    public MultiTenantAuthenticationManager(TenantService tenantService, JwtDecoder multiTenantJwtDecoder) {
        this.tenantService = tenantService;
        this.multiTenantJwtDecoder = multiTenantJwtDecoder;
    }

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
                    converter.setJwtGrantedAuthoritiesConverter(new KeycloakAuthoritiesConverter());
                    provider.setJwtAuthenticationConverter(converter);
                    return provider;
                })
                .orElseThrow(() -> new IllegalArgumentException("Unknown tenant"))::authenticate;
    }

}
