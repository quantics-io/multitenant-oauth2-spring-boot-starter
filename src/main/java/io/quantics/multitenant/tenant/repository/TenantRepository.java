package io.quantics.multitenant.tenant.repository;

import io.quantics.multitenant.tenant.model.Tenant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {

    Optional<Tenant> findByIssuer(String issuer);
}
