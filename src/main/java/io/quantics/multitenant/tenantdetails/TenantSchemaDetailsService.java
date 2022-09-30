package io.quantics.multitenant.tenantdetails;

import java.util.Optional;


public interface TenantSchemaDetailsService extends TenantDetailsService {

    Iterable<? extends TenantSchemaDetails> getAll();

    Optional<? extends TenantSchemaDetails> getById(String id);

    Optional<? extends TenantSchemaDetails> getByIssuer(String issuer);

}
