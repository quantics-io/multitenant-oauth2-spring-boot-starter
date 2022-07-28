package io.quantics.multitenant.oauth2.config;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.lang.reflect.InvocationTargetException;

/**
 * A {@link JwtAuthenticationConverter} that can be configured via passing a class name. The class of the converter
 * must extend {@link AbstractJwtGrantedAuthoritiesConverter}.
 */
public final class MultiTenantJwtAuthenticationConverter extends JwtAuthenticationConverter {

    public MultiTenantJwtAuthenticationConverter(String className) {
        try {
            Class<?> converterClass = Class.forName(className);
            var converter = (AbstractJwtGrantedAuthoritiesConverter) converterClass.getConstructor().newInstance();
            setJwtGrantedAuthoritiesConverter(converter);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
