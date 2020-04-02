package io.quantics.multitenant.tenant.model;

public interface TenantDetails {

    String getId();

    String getName();

    String getIssuer();

    String getJwkSetUrl();

}
