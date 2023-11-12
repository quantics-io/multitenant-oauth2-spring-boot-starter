package io.quantics.multitenant.oauth2.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures a {@link SecurityFilterChain} when <i>jwt</i> is used as the mode for resolving the tenant.
 * An {@link AuthenticationManagerResolver} takes care of performing the authentication using multiple
 * authentication managers.
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
    @Conditional(JwtCondition.class)
    @ConditionalOnClass(AuthenticationManagerResolver.class)
    public SecurityFilterChain multiTenantJwtFilterChain(
            HttpSecurity http, AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver)
            throws Exception {

        http.authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
        );
        http.oauth2ResourceServer(oauth2 -> oauth2
                .authenticationManagerResolver(authenticationManagerResolver)
        );

        return http.build();
    }

}
