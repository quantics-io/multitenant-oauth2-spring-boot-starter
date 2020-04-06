package io.quantics.multitenant.config.oauth2;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * Abstract converter for converting a {@link Jwt} to a {@link Collection} of
 * {@link GrantedAuthority GrantedAuthorities}.
 */
public abstract class AbstractJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

}
