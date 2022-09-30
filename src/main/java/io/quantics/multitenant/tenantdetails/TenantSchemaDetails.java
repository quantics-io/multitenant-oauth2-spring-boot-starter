package io.quantics.multitenant.tenantdetails;

/**
 * Provides core information about a tenant in a multi-tenant application where each tenant has their own schema.
 *
 */
public interface TenantSchemaDetails extends TenantDetails {

    String getSchema();

}
