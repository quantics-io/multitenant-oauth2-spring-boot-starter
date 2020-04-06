package io.quantics.multitenant.tenantdetails;

/**
 * Provides core information about a tenant in a multi-tenant application.
 *
 * @see TenantDetailsService
 */
public interface TenantDetails {

    String getId();

    String getIssuer();

    String getJwkSetUrl();

}
