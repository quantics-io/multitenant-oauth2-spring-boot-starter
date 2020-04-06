package io.quantics.multitenant.config.oauth2;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Condition for creating header-based beans.
 */
class HeaderCondition extends AllNestedConditions {

    HeaderCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "enabled",
            havingValue = "true")
    static class OnEnabled { }

    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "resolve-mode",
            havingValue = "header")
    static class OnResolveModeHeader { }

}
