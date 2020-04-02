package io.quantics.multitenant.tenant.service;

import io.quantics.multitenant.tenant.model.TenantDetails;

import java.util.Optional;

public interface TenantDetailsService {

    Optional<? extends TenantDetails> getByIssuer(String issuer);

}
