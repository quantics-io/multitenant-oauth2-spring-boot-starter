package io.quantics.multitenant.config.oauth2;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

class JwtConverterCondition extends AllNestedConditions {

    JwtConverterCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "enabled",
            havingValue = "true")
    static class OnEnabled { }

    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant", name = "authorities-converter")
    static class OnAuthoritiesConverter { }

}
