package io.quantics.multitenant.tenantdetails;

/**
 * Provides core information about a tenant in a multi-tenant application where each tenant has its own database.
 *
 */
public interface TenantDatabaseDetails extends TenantDetails {

    String getDatabase();

}
