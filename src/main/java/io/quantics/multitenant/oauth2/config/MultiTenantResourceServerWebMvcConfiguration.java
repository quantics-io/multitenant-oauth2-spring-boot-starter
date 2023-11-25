package io.quantics.multitenant.oauth2.config;

import io.quantics.multitenant.TenantContext;
import io.quantics.multitenant.tenantdetails.TenantDetails;
import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

/**
 * Configures a {@link WebMvcConfigurer} with a {@link HandlerInterceptor}. The interceptor is used for setting
 * the current tenant in the {@link TenantContext} automatically for each request.
 * <ul>
 *     <li>If <i>header</i> is used as the mode for resolving the tenant, the current tenant is resolved from the
 *         configured header name.
 *     <li>If <i>jwt</i> is used as the mode for resolving the tenant, the current tenant is resolved from the token's
 *         <i>iss</i> claim.
 * </ul>
 */
@Configuration
public class MultiTenantResourceServerWebMvcConfiguration {

    private static final Log logger = LogFactory.getLog(WebMvcConfigurer.class);

    @Bean({ "multiTenantHeaderInterceptor", "multiTenantInterceptor" })
    @Conditional(HeaderCondition.class)
    HandlerInterceptor multiTenantHeaderInterceptor(MultiTenantResourceServerProperties properties,
                                                    TenantDetailsService tenantService) {
        return new HandlerInterceptor() {

            @Override
            public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull Object handler) throws IOException {
                String tenantId = request.getHeader(properties.getHeader().getHeaderName());
                if (tenantId != null) {
                    var tenant = tenantService.getById(tenantId);
                    if (tenant.isPresent()) {
                        logger.debug("Set TenantContext: " + tenant.get().getId());
                        TenantContext.setTenantId(tenant.get().getId());
                        return true;
                    }
                }

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            @Override
            public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                   @NonNull Object handler, ModelAndView modelAndView) {
                if (TenantContext.getTenantId() != null) {
                    logger.debug("Clear TenantContext: " + TenantContext.getTenantId());
                    TenantContext.clear();
                }
            }

        };
    }

    @Bean({ "multiTenantJwtInterceptor", "multiTenantInterceptor" })
    @Conditional(JwtCondition.class)
    HandlerInterceptor multiTenantJwtInterceptor(TenantDetailsService tenantService) {
        return new HandlerInterceptor() {

            @Override
            public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull Object handler) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof AbstractOAuth2TokenAuthenticationToken<?> token) {
                    String issuer = (String) token.getTokenAttributes().get("iss");
                    TenantDetails tenant = tenantService.getByIssuer(issuer)
                            .orElseThrow(() -> new IllegalArgumentException("Tenant not found for issuer: " + issuer));
                    logger.debug("Set TenantContext: " + tenant.getId());
                    TenantContext.setTenantId(tenant.getId());
                }
                return true;
            }

            @Override
            public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                   @NonNull Object handler, ModelAndView modelAndView) {
                if (TenantContext.getTenantId() != null) {
                    logger.debug("Clear TenantContext: " + TenantContext.getTenantId());
                    TenantContext.clear();
                }
            }
        };
    }

    @Bean
    @ConditionalOnBean(value = HandlerInterceptor.class, name = "multiTenantInterceptor")
    WebMvcConfigurer multiTenantWebMvcConfigurer() {
        return new WebMvcConfigurer() {

            @Autowired
            private HandlerInterceptor multiTenantInterceptor;

            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(multiTenantInterceptor);
            }
        };
    }

}
