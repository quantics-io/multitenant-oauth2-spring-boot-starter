package io.quantics.example.multitenancy.config.db;

import io.quantics.multitenant.util.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Resolver for translating the current tenant-id into the schema to be used for the data source.
 */
@Component
public class CurrentTenantResolver implements CurrentTenantIdentifierResolver {

    public static final String DEFAULT_SCHEMA = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenantId() != null
                ? TenantContext.getTenantId()
                : DEFAULT_SCHEMA;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}
