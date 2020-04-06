package io.quantics.multitenant.config.oauth2;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Condition for creating jwt-based beans that use an authorities converter.
 */
class AuthoritiesConverterCondition extends AllNestedConditions {

    AuthoritiesConverterCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant.jwt", name = "authorities-converter")
    static class OnAuthoritiesConverter { }

}
