package io.quantics.multitenant.config.oauth2;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ConfigurationCondition;

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
