package io.quantics.multitenant;

import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.quantics.multitenant.tenant.controller.HomeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest({
        "spring.security.oauth2.resourceserver.multitenant.enabled=true",
        "spring.security.oauth2.resourceserver.multitenant.resolve-mode=jwt",
})
@AutoConfigureMockMvc
@WithMockUser(username = "test")
class MultiTenantJwtApplicationTests {

    @Autowired
    private HomeController controller;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
        assertThat(context.getBean(JWTClaimsSetAwareJWSKeySelector.class)).isNotNull();
        assertThat(context.getBean(JWTProcessor.class)).isNotNull();
        assertThat(context.getBean(OAuth2TokenValidator.class)).isNotNull();
        assertThat(context.getBean(JwtDecoder.class)).isNotNull();
        assertThat(context.getBean("multiTenantJwtDecoderWebSecurity", WebSecurityConfigurerAdapter.class))
                .isNotNull();
        assertThat(context.getBean("multiTenantJwtInterceptor", HandlerInterceptorAdapter.class)).isNotNull();
        assertThat(context.getBean("multiTenantWebMvcConfigurer", WebMvcConfigurer.class)).isNotNull();
    }

    @Test
    void shouldReturnHelloWorld() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello World")));
    }

}
