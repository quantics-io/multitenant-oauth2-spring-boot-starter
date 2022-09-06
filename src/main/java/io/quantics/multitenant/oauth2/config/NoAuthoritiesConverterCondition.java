package io.quantics.multitenant.oauth2.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;

/**
 * Condition for creating jwt-based beans that use an authorities converter.
 */
class NoAuthoritiesConverterCondition extends NoneNestedConditions {

    NoAuthoritiesConverterCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.multitenant.jwt", name = "authorities-converter")
    static class OnAuthoritiesConverter { }

}
