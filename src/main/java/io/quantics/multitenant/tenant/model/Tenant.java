package io.quantics.multitenant.tenant.model;

import io.quantics.multitenant.config.db.TenantSchemaResolver;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "tenant", schema = TenantSchemaResolver.DEFAULT_SCHEMA)
public class Tenant {

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
}
