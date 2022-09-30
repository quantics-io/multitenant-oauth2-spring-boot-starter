package io.quantics.multitenant.tenantdetails;

import java.util.Optional;


public interface TenantDatabaseDetailsService extends TenantDetailsService {

    Iterable<? extends TenantDatabaseDetails> getAll();

    Optional<? extends TenantDatabaseDetails> getById(String id);

    Optional<? extends TenantDatabaseDetails> getByIssuer(String issuer);

}
