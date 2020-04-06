package io.quantics.multitenant.tenant;

import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantService implements TenantDetailsService {

    private final TenantRepository repository;

    @Autowired
    public TenantService(TenantRepository repository) {
        this.repository = repository;
    }

    public Iterable<Tenant> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Tenant> getByIssuer(String issuer) {
        return repository.findByIssuer(issuer);
    }

}
