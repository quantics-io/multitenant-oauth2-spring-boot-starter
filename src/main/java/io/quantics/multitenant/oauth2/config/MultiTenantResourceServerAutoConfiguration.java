package io.quantics.multitenant.oauth2.config;

import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for multi-tenant resource server support.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({ WebMvcAutoConfiguration.class, SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class })
@EnableConfigurationProperties(MultiTenantResourceServerProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class MultiTenantResourceServerAutoConfiguration {

    @Configuration
    @ConditionalOnClass({ JWTClaimsSetAwareJWSKeySelector.class, JWTProcessor.class, OAuth2TokenValidator.class,
            JwtDecoder.class })
    @Import(MultiTenantResourceServerJwtConfiguration.class)
    static class JwtConfiguration { }

    @Configuration
    @ConditionalOnClass(WebSecurityConfigurerAdapter.class)
    @Import(MultiTenantResourceServerWebSecurityConfiguration.class)
    static class WebSecurityConfiguration { }


    @Configuration
    @ConditionalOnClass({ HandlerInterceptor.class, WebMvcConfigurer.class })
    @Import(MultiTenantResourceServerWebMvcConfiguration.class)
    static class WebMvcConfiguration { }

}
