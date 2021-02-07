package io.quantics.multitenant.oauth2.config;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ConfigurationCondition;

/**
 * Condition for creating jwt-based beans.
 */
class JwtCondition extends AllNestedConditions {

    JwtCondition() {
        super(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "enabled",
            havingValue = "true")
    static class OnEnabled { }

    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "resolve-mode",
            havingValue = "jwt")
    static class OnResolveModeJwt { }

}
