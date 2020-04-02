package io.quantics.multitenant.config.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@ConfigurationProperties("spring.security.oauth2.resourceserver.multitenant")
public class MultiTenantResourceServerProperties {

    private boolean enabled = false;
    private boolean useHeader = false;
    private boolean useToken = false;
    private String authoritiesConverter;

    @PostConstruct
    void validate() throws ClassNotFoundException {
        if (enabled) {
            if (useHeader && useToken) {
                throw new IllegalStateException("Only one of use-header and use-token should be configured");
            }
            if (!useHeader && !useToken) {
                throw new IllegalStateException("One of use-header and use-token should be configured");
            }
        }
        if (authoritiesConverter != null
                && !AbstractJwtGrantedAuthoritiesConverter.class.isAssignableFrom(Class.forName(authoritiesConverter))) {
            throw new IllegalStateException("converter must implement "
                    + AbstractJwtGrantedAuthoritiesConverter.class.getName());
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isUseHeader() {
        return useHeader;
    }

    public void setUseHeader(boolean useHeader) {
        this.useHeader = useHeader;
    }

    public boolean isUseToken() {
        return useToken;
    }

    public void setUseToken(boolean useToken) {
        this.useToken = useToken;
    }

    public String getAuthoritiesConverter() {
        return authoritiesConverter;
    }

    public void setAuthoritiesConverter(String authoritiesConverter) {
        this.authoritiesConverter = authoritiesConverter;
    }
}
