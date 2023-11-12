package io.quantics.multitenant.oauth2.config;

import com.nimbusds.jwt.JWTParser;
import io.quantics.multitenant.tenantdetails.TenantDetails;
import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A tenant-aware implementation of an AuthenticationManagerResolver that holds a repository of all
 * authentication managers. Each manager is responsible for resolving the authentication of a
 * specific tenant.
 *
 * @see AuthenticationManagerResolver
 */
public class MultiTenantAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    private final TenantDetailsService tenantService;
    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter authenticationConverter;
    private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();
    private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

    public MultiTenantAuthenticationManagerResolver(TenantDetailsService tenantService, JwtDecoder jwtDecoder) {
        this.tenantService = tenantService;
        this.jwtDecoder = jwtDecoder;
        this.authenticationConverter = null;
    }

    public MultiTenantAuthenticationManagerResolver(TenantDetailsService tenantService, JwtDecoder jwtDecoder,
                                                    JwtAuthenticationConverter authenticationConverter) {
        this.tenantService = tenantService;
        this.jwtDecoder = jwtDecoder;
        this.authenticationConverter = authenticationConverter;
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        return authenticationManagers.computeIfAbsent(toTenant(request), this::fromTenant);
    }

    private String toTenant(HttpServletRequest request) {
        try {
            String token = resolver.resolve(request);
            return JWTParser.parse(token).getJWTClaimsSet().getIssuer();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private AuthenticationManager fromTenant(String tenant) {
        return tenantService.getByIssuer(tenant)
                .map(TenantDetails::getIssuer)
                .map(i -> {
                    var provider = new JwtAuthenticationProvider(jwtDecoder);
                    if (authenticationConverter != null) {
                        provider.setJwtAuthenticationConverter(authenticationConverter);
                    }
                    return provider;
                })
                .orElseThrow(() -> new InvalidBearerTokenException("Unknown tenant: " + tenant))
                ::authenticate;
    }

}
