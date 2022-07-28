package io.quantics.multitenant.oauth2.config;

import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures a {@link SecurityFilterChain} when <i>jwt</i> is used as the mode for resolving the tenant.
 * If a JWT converter is configured as well, then this converter is configured using a
 * {@link JwtAuthenticationConverter}.
 * If <i>header</i> is used as resolve mode, then all requests are permitted.
 */
@Configuration
public class MultiTenantResourceServerWebSecurityConfiguration {

    @Bean
    @Conditional(HeaderCondition.class)
    public SecurityFilterChain multiTenantHeaderFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
        );

        return http.build();
    }

    @Bean
    @Conditional({ JwtCondition.class, NoAuthoritiesConverterCondition.class })
    public SecurityFilterChain multiTenantJwtFilterChain(
            HttpSecurity http, TenantDetailsService tenantService, JwtDecoder multiTenantJwtDecoder) throws Exception {


        http.authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
        );
        http.oauth2ResourceServer(oauth2 -> oauth2
                .authenticationManagerResolver(
                        new MultiTenantAuthenticationManagerResolver(tenantService, multiTenantJwtDecoder)
                )
        );

        return http.build();
    }

    @Bean
    @Conditional({ JwtCondition.class, AuthoritiesConverterCondition.class })
    public SecurityFilterChain multiTenantJwtAuthoritiesConverterFilterChain(
            HttpSecurity http, TenantDetailsService tenantService, JwtDecoder multiTenantJwtDecoder,
            MultiTenantResourceServerProperties properties) throws Exception {

        http.authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
        );
        http.oauth2ResourceServer(oauth2 -> oauth2
                .authenticationManagerResolver(
                        new MultiTenantAuthenticationManagerResolver(tenantService, multiTenantJwtDecoder,
                                new MultiTenantJwtAuthenticationConverter(properties.getJwt().getAuthoritiesConverter())
                        )
                )
        );

        return http.build();
    }

}
