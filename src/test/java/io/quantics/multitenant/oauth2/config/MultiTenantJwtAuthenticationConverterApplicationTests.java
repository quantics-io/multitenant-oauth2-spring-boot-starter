package io.quantics.multitenant.oauth2.config;

import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.quantics.multitenant.app.TestApplication;
import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class, properties = {
        "spring.security.oauth2.resourceserver.multitenant.enabled=true",
        "spring.security.oauth2.resourceserver.multitenant.resolve-mode=jwt",
        "spring.security.oauth2.resourceserver.multitenant.jwt.authorities-converter="
                + "io.quantics.multitenant.oauth2.config.KeycloakRealmAuthoritiesConverter",
})
@AutoConfigureMockMvc
class MultiTenantJwtAuthenticationConverterApplicationTests {

    @Autowired
    private ApplicationContext context;

    @MockBean
    private TenantDetailsService tenantService;

    @Test
    void contextLoads() {
        assertThat(context.getBean(JWTClaimsSetAwareJWSKeySelector.class)).isNotNull();
        assertThat(context.getBean(JWTProcessor.class)).isNotNull();
        assertThat(context.getBean(OAuth2TokenValidator.class)).isNotNull();
        assertThat(context.getBean(JwtDecoder.class)).isNotNull();
        assertThat(context.getBean("multiTenantJwtFilterChain", SecurityFilterChain.class)).isNotNull();
        assertThat(context.getBean("multiTenantJwtAuthoritiesConverterResolver",
                AuthenticationManagerResolver.class)).isNotNull();
        assertThat(context.getBean("multiTenantJwtInterceptor", HandlerInterceptor.class)).isNotNull();
        assertThat(context.getBean("multiTenantWebMvcConfigurer", WebMvcConfigurer.class)).isNotNull();
    }

}
