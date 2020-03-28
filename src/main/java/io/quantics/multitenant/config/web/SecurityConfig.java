package io.quantics.multitenant.config.web;

import io.quantics.multitenant.config.web.oauth2.MultiTenantAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final MultiTenantAuthenticationManager authenticationManager;

    @Autowired
    public SecurityConfig(MultiTenantAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(a -> a
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .authenticationManagerResolver(this.authenticationManager));
    }
}
