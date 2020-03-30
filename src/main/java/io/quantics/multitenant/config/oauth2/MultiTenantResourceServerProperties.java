package io.quantics.multitenant.config.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@ConfigurationProperties("spring.security.oauth2.resourceserver.multitenant")
public class MultiTenantResourceServerProperties {

    private boolean enabled = false;
    private boolean useHeader = false;
    private boolean useToken = false;

    @PostConstruct
    void validate() {
        if (enabled) {
            if (useHeader && useToken) {
                throw new IllegalStateException("Only one of use-header and use-token should be configured");
            }
            if (!useHeader && !useToken) {
                throw new IllegalStateException("One of use-header and use-token should be configured");
            }
        }
        if (!enabled && (useHeader || useToken)) {
            throw new IllegalStateException("Set multitenant=true to activate multi-tenant support");
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
}
