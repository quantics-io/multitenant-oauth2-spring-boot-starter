package io.quantics.multitenant.tenant.service;

import io.quantics.multitenant.config.oauth2.KeycloakRealmAuthoritiesConverter;
import io.quantics.multitenant.tenant.model.Tenant;
import io.quantics.multitenant.tenant.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TenantService {

    private final TenantRepository repository;
    private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();
    private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter =
            new KeycloakRealmAuthoritiesConverter();

    @Autowired
    public TenantService(TenantRepository repository) {
        this.repository = repository;
    }

    public Iterable<Tenant> getAll() {
        return repository.findAll();
    }

    public Optional<Tenant> getByIssuer(String issuer) {
        return repository.findByIssuer(issuer);
    }

    public AuthenticationManager getAuthenticationManager(String issuer, JwtDecoder jwtDecoder) {
        return authenticationManagers.computeIfAbsent(issuer, iss -> fromTenant(jwtDecoder));
    }

    private AuthenticationManager fromTenant(JwtDecoder jwtDecoder) {
        JwtAuthenticationProvider authenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        authenticationProvider.setJwtAuthenticationConverter(authenticationConverter);
        return authenticationProvider::authenticate;
    }
}
