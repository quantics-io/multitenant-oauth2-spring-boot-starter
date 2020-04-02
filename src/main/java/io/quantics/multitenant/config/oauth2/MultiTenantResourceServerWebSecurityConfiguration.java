package io.quantics.multitenant.config.oauth2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.lang.reflect.InvocationTargetException;

@Configuration
public class MultiTenantResourceServerWebSecurityConfiguration {

    @Bean
    @Conditional(JwtConverterCondition.class)
    WebSecurityConfigurerAdapter multiTenantJwtAuthenticationConverterWebSecurity(
            MultiTenantResourceServerProperties properties) {
        return new WebSecurityConfigurerAdapter() {

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http.authorizeRequests(authorize -> authorize
                        .anyRequest().authenticated());
                http.oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        ));
            }

            private JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                try {
                    Class<?> converterClass = Class.forName(properties.getAuthoritiesConverter());
                    AbstractJwtGrantedAuthoritiesConverter converter = (AbstractJwtGrantedAuthoritiesConverter)
                            converterClass.getConstructor().newInstance();
                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter);
                    return jwtAuthenticationConverter;
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                        | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    @ConditionalOnBean(JwtDecoder.class)
    WebSecurityConfigurerAdapter multiTenantJwtDecoderWebSecurity(JwtDecoder multiTenantJwtDecoder) {
        return new WebSecurityConfigurerAdapter() {

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http.authorizeRequests(authorize -> authorize
                        .anyRequest().authenticated());
                http.oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(multiTenantJwtDecoder)));
            }
        };
    }

}
