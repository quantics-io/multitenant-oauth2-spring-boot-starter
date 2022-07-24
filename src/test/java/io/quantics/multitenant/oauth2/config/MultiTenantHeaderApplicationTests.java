package io.quantics.multitenant.oauth2.config;

import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.quantics.multitenant.app.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class, properties = {
        "spring.security.oauth2.resourceserver.multitenant.enabled=true",
        "spring.security.oauth2.resourceserver.multitenant.resolve-mode=header",
        "spring.security.oauth2.resourceserver.multitenant.header.header-name="
                + MultiTenantHeaderApplicationTests.HEADER_NAME,
})
@AutoConfigureMockMvc
class MultiTenantHeaderApplicationTests {

    static final String HEADER_NAME = "TEST-TENANT-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(JWTClaimsSetAwareJWSKeySelector.class));
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(JWTProcessor.class));
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(OAuth2TokenValidator.class));
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(JwtDecoder.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> context.getBean("multiTenantJwtAuthenticationConverterWebSecurity",
                        WebSecurityConfigurerAdapter.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> context.getBean("multiTenantJwtDecoderWebSecurity", WebSecurityConfigurerAdapter.class));
        assertThat(context.getBean("multiTenantHeaderInterceptor", HandlerInterceptor.class)).isNotNull();
        assertThat(context.getBean("multiTenantWebMvcConfigurer", WebMvcConfigurer.class)).isNotNull();
    }

    @Test
    void shouldReturnHelloWorld() throws Exception {
        String tenantId = "test-tenant";
        mockMvc.perform(get("/").header(HEADER_NAME, tenantId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello World from " + tenantId)));
    }

    @Test
    void shouldReturnGeneralHelloWorld() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello World!")))
                .andExpect(content().string(not(containsString("Hello World from"))));
    }

}
