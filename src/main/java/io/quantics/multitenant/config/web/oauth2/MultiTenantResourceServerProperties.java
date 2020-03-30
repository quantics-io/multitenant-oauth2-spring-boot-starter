package io.quantics.multitenant.config.web.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.security.oauth2.resourceserver.multitenant")
public class MultiTenantResourceServerProperties {

    private boolean enabled = false;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
