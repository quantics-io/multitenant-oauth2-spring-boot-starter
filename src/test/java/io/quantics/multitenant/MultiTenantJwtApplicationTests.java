package io.quantics.multitenant;

import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest({
        "spring.security.oauth2.resourceserver.multitenant.enabled=true",
        "spring.security.oauth2.resourceserver.multitenant.resolve-mode=jwt",
        "spring.security.oauth2.resourceserver.multitenant.jwt.authorities-converter="
                + "io.quantics.multitenant.oauth2.config.KeycloakRealmAuthoritiesConverter",
})
@AutoConfigureMockMvc
@WithMockUser(username = "test")
class MultiTenantJwtApplicationTests {

    private static final String JWT_TEST_TENANT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vdGVzdC5kZXYvdGVzdC10ZW5hbnQiLCJzdWIiOiIxM2NlZDc2YS1kZDUxLTQ1MzktOTU1OC0zNTBhNWRjMzZhMDMiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsicm9sZTEiLCJyb2xlMiJdfSwibmFtZSI6IlVzZXIgTmFtZSJ9.XHQ0tJwFxX_pp-gMz95VAturg-SJBREGa92OB8_zlf4";
    private static final String JWT_UNKNOWN_TENANT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vdGVzdC5kZXYvdW5rbm93bi10ZW5hbnQiLCJzdWIiOiJkMmNlNjJhOC0yMDE2LTRlY2QtYWNkYi1lNjZlNmQyMWI0YjYiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsicm9sZTEiLCJyb2xlMiJdfSwibmFtZSI6IlVzZXIgTmFtZSJ9.Rro_8qgEt9eFK_Zfz40PuciyQCpmj71DSeDnHgvQNz0";

    @Autowired
    private HomeController controller;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @MockBean
    private JwtDecoder multiTenantJwtDecoder;

    @MockBean
    private TenantDetailsService tenantService;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
        assertThat(context.getBean(JWTClaimsSetAwareJWSKeySelector.class)).isNotNull();
        assertThat(context.getBean(JWTProcessor.class)).isNotNull();
        assertThat(context.getBean(OAuth2TokenValidator.class)).isNotNull();
        assertThat(context.getBean(JwtDecoder.class)).isNotNull();
        assertThat(context.getBean("multiTenantJwtAuthenticationConverterWebSecurity",
                WebSecurityConfigurerAdapter.class)).isNotNull();
        assertThat(context.getBean("multiTenantJwtInterceptor", HandlerInterceptorAdapter.class)).isNotNull();
        assertThat(context.getBean("multiTenantWebMvcConfigurer", WebMvcConfigurer.class)).isNotNull();
    }

    @Test
    void getWithJwtFromKnownTenant_shouldReturnHelloWorld() throws Exception {
        String issuer = "http://test.dev/test-tenant";
        Tenant tenant = Tenant.builder()
                .id("test-tenant")
                .issuer(issuer)
                .build();

        Mockito.doReturn(Optional.of(tenant))
                .when(tenantService).getByIssuer(issuer);

        Mockito.when(multiTenantJwtDecoder.decode(JWT_TEST_TENANT))
                .thenReturn(new Jwt(JWT_TEST_TENANT, Instant.now(), Instant.now().plusSeconds(60),
                        Map.of("alg", "HS256", "typ", "JWT"),
                        Map.of("realm_access", Map.of("roles", List.of("role1", "role2")),
                                "iss", issuer,
                                "sub", "13ced76a-dd51-4539-9558-350a5dc36a03")));

        mockMvc.perform(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TEST_TENANT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello World from test-tenant")));
    }

    @Test
    void getWithJwtFromUnknownTenant_shouldReturnUnauthorized() throws Exception {
        String issuer = "http://test.dev/unknown-tenant";

        Mockito.doReturn(Optional.empty())
                .when(tenantService).getByIssuer(issuer);

        Mockito.when(multiTenantJwtDecoder.decode(JWT_UNKNOWN_TENANT))
                .thenThrow(new JwtException("Unknown tenant"));

        mockMvc.perform(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_UNKNOWN_TENANT))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}
