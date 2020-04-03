package io.quantics.multitenant.config.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@ConfigurationProperties("spring.security.oauth2.resourceserver.multitenant")
public class MultiTenantResourceServerProperties {

    public static final boolean DEFAULT_ENABLED = false;
    public static final ResolveMode DEFAULT_RESOLVE_MODE = ResolveMode.JWT;

    private boolean enabled = DEFAULT_ENABLED;
    private ResolveMode resolveMode = DEFAULT_RESOLVE_MODE;
    private String authoritiesConverter;

    @PostConstruct
    void validate() throws ClassNotFoundException {
        if (authoritiesConverter != null
                && !AbstractJwtGrantedAuthoritiesConverter.class.isAssignableFrom(Class.forName(authoritiesConverter))) {
            throw new IllegalStateException("authoritiesConverter must implement "
                    + AbstractJwtGrantedAuthoritiesConverter.class.getName());
        }
    }

    public enum ResolveMode {
        JWT,
        HEADER,
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ResolveMode getResolveMode() {
        return resolveMode;
    }

    public void setResolveMode(ResolveMode resolveMode) {
        this.resolveMode = resolveMode;
    }

    public String getAuthoritiesConverter() {
        return authoritiesConverter;
    }

    public void setAuthoritiesConverter(String authoritiesConverter) {
        this.authoritiesConverter = authoritiesConverter;
    }
}
