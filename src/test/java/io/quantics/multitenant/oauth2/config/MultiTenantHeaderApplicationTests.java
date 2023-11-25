package io.quantics.multitenant.oauth2.config;

import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.quantics.multitenant.app.TestApplication;
import io.quantics.multitenant.tenant.Tenant;
import io.quantics.multitenant.tenantdetails.TenantDetails;
import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
@Import(MultiTenantHeaderTestConfiguration.class)
@AutoConfigureMockMvc
class MultiTenantHeaderApplicationTests {

    static final String HEADER_NAME = "TEST-TENANT-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @MockBean
    private TenantDetailsService tenantService;

    @Test
    void contextLoads() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(JWTClaimsSetAwareJWSKeySelector.class));
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(JWTProcessor.class));
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(OAuth2TokenValidator.class));
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(JwtDecoder.class));
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(AuthenticationManagerResolver.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> context.getBean("multiTenantJwtFilterChain", SecurityFilterChain.class));
        assertThat(context.getBean("multiTenantHeaderInterceptor", HandlerInterceptor.class)).isNotNull();
        assertThat(context.getBean("multiTenantWebMvcConfigurer", WebMvcConfigurer.class)).isNotNull();
    }

    @Test
    void getWithKnownTenant_shouldReturnHelloWorld() throws Exception {
        String tenantId = "test-tenant";
        TenantDetails tenant = new Tenant(tenantId, "http://test.dev/test-tenant");

        Mockito.doReturn(Optional.of(tenant))
                .when(tenantService).getById(tenantId);

        mockMvc.perform(get("/").header(HEADER_NAME, tenantId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello World from " + tenantId)));
    }

    @Test
    void getWithUnknownTenant_shouldReturnUnauthorized() throws Exception {
        String tenantId = "test-tenant";

        Mockito.doReturn(Optional.empty())
                .when(tenantService).getById(tenantId);

        mockMvc.perform(get("/").header(HEADER_NAME, tenantId))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getWithoutTenant_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}
