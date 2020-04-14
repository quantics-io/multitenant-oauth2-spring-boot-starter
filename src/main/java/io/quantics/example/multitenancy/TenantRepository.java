package io.quantics.example.multitenancy;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {

    Optional<Tenant> findByIssuer(String issuer);

}
