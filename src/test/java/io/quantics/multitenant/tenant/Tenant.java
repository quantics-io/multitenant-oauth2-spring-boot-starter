package io.quantics.multitenant.tenant;

import io.quantics.multitenant.tenantdetails.TenantDetails;

public class Tenant implements TenantDetails {

    private final String id;

    public Tenant(String id, String issuer) {
        this.id = id;
        this.issuer = issuer;
    }

    private final String issuer;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public String getJwkSetUrl() {
        return issuer + "/protocol/openid-connect/certs";
    }

}
