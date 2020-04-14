package io.quantics.example.multitenancy;

import io.quantics.example.multitenancy.config.db.CurrentTenantResolver;
import io.quantics.multitenant.tenantdetails.TenantDetails;
import io.quantics.multitenant.util.UrlUtils;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
@Table(name = "tenant", schema = CurrentTenantResolver.DEFAULT_SCHEMA)
public class Tenant implements TenantDetails {

    @Id
    @Column(name = "id", updatable = false)
    private String id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "schema", nullable = false)
    private String schema;

    @NotNull
    @Column(name = "issuer", nullable = false)
    private String issuer;


    @Override
    public String getJwkSetUrl() {
        return UrlUtils.removeTrailingSlash(this.issuer) + "/protocol/openid-connect/certs";
    }

}
