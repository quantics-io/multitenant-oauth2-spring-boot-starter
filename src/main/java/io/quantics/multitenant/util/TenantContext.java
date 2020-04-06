package io.quantics.multitenant.util;

/**
 * Provides information about the current tenant in a thread-safe way.
 */
public class TenantContext {

    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

    public static String getTenantId() {
        return TENANT.get();
    }

    public static void setTenantId(String tenantId) {
        TENANT.set(tenantId);
    }

    public static void clear() {
        TENANT.remove();
    }

}
