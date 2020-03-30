package io.quantics.multitenant.config.web.oauth2;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import io.quantics.multitenant.tenant.model.Tenant;
import io.quantics.multitenant.tenant.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnBean(name = "multiTenantJwtProcessor")
public class MultiTenantJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private final TenantService tenantService;
    private final Map<String, JWSKeySelector<SecurityContext>> selectors = new ConcurrentHashMap<>();

    @Autowired
    public MultiTenantJWSKeySelector(TenantService tenantService) {
        this.tenantService = tenantService;
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

    private JWSKeySelector<SecurityContext> fromTenant(String tenant) {
        return this.tenantService.getByIssuer(tenant)
                .map(Tenant::getJwkSetUrl)
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
