package io.quantics.multitenant.oauth2.config;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.lang.reflect.InvocationTargetException;

public final class MultiTenantJwtAuthenticationConverter extends JwtAuthenticationConverter {

    public MultiTenantJwtAuthenticationConverter(String className) {
        try {
            Class<?> converterClass = Class.forName(className);
            AbstractJwtGrantedAuthoritiesConverter converter = (AbstractJwtGrantedAuthoritiesConverter)
                    converterClass.getConstructor().newInstance();
            setJwtGrantedAuthoritiesConverter(converter);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
