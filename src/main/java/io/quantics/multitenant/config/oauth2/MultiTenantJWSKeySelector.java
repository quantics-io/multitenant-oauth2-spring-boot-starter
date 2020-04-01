package io.quantics.multitenant.config.oauth2;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;

import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MultiTenantJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private final Function<String, Optional<String>> issuerToJwkSetUrl;
    private final Map<String, JWSKeySelector<SecurityContext>> selectors = new ConcurrentHashMap<>();

    public MultiTenantJWSKeySelector(Function<String, Optional<String>> issuerToJwkSetUrl) {
        this.issuerToJwkSetUrl = issuerToJwkSetUrl;
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet,
                                          SecurityContext securityContext) throws KeySourceException {
        return this.selectors.computeIfAbsent(toTenant(jwtClaimsSet), this::fromTenant)
                .selectJWSKeys(jwsHeader, securityContext);
    }

    private String toTenant(JWTClaimsSet claimSet) {
        return claimSet.getIssuer();
    }

    private JWSKeySelector<SecurityContext> fromTenant(String issuer) {
        return this.issuerToJwkSetUrl.apply(issuer)
                .map(this::fromUri)
                .orElseThrow(() -> new IllegalArgumentException("Unknown tenant"));
    }

    private JWSKeySelector<SecurityContext> fromUri(String uri) {
        try {
            return JWSAlgorithmFamilyJWSKeySelector.fromJWKSetURL(new URL(uri));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
