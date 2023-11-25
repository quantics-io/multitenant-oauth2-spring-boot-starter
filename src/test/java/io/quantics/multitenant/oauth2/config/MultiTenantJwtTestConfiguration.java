package io.quantics.multitenant.oauth2.config;

import io.quantics.multitenant.tenantdetails.TenantDetails;
import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@TestConfiguration
public class MultiTenantJwtTestConfiguration {

    @Bean
    TenantDetailsService tenantService() {
        return new TenantDetailsService() {
            
            @Override
            public Iterable<? extends TenantDetails> getAll() {
                return null;
            }

            @Override
            public Optional<? extends TenantDetails> getById(String id) {
                return Optional.empty();
            }

            @Override
            public Optional<? extends TenantDetails> getByIssuer(String issuer) {
                return Optional.empty();
            }

        };

    }

}
