package io.quantics.multitenant.oauth2.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter for converting a Keycloak-issued {@link Jwt} into {@link GrantedAuthority GrantedAuthorities}.
 * The converter uses the <i>roles</i> inside the <i>realm_access</i> claim for mapping to granted authorities.
 * Each role is prefixed with <i>ROLE_</i> for ensuring compatibility with the Spring framework.
 *
 * @see <a href="https://www.keycloak.org">https://www.keycloak.org</a>
 */
public final class KeycloakRealmAuthoritiesConverter extends AbstractJwtGrantedAuthoritiesConverter {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(final Jwt jwt) {
        final Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        return ((List<String>) realmAccess.get("roles")).stream()
                .map(r -> "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
