package io.quantics.multitenant.config.oauth2;

import io.quantics.multitenant.tenantdetails.TenantDetails;
import io.quantics.multitenant.tenantdetails.TenantDetailsService;
import io.quantics.multitenant.util.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Configures a {@link WebMvcConfigurer} with a {@link HandlerInterceptorAdapter}. The interceptor is used for setting
 * the current tenant in the {@link TenantContext} automatically for each request.
 * <ul>
 *     <li>If <i>header</i> is used as the mode for resolving the tenant, the current tenant is resolved from the
 *         configured header name.
 *     <li>If <i>jwt</i> is used as the mode for resolving the tenant, the current tenant is resolved from the token's
 *         <i>iss</i> claim.
 * </ul>
 */
@Configuration
@ConditionalOnMissingBean(WebMvcConfigurer.class)
@Slf4j
public class MultiTenantResourceServerWebMvcConfiguration {

    @Bean
    @Conditional(HeaderCondition.class)
    HandlerInterceptorAdapter multiTenantHeaderInterceptor(MultiTenantResourceServerProperties properties) {
        return new HandlerInterceptorAdapter() {

            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String tenantId = request.getHeader(properties.getHeader().getHeaderName());
                log.debug("Set TenantContext: {}", tenantId);
                TenantContext.setTenantId(tenantId);
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                                   ModelAndView modelAndView) {
                log.debug("Clear TenantContext: {}", TenantContext.getTenantId());
                TenantContext.clear();
            }

        };
    }

    @Bean
    @Conditional(JwtCondition.class)
    HandlerInterceptorAdapter multiTenantJwtInterceptor(TenantDetailsService tenantService) {
        return new HandlerInterceptorAdapter() {

            @Override
            @SuppressWarnings("rawtypes")
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof AbstractOAuth2TokenAuthenticationToken) {
                    AbstractOAuth2TokenAuthenticationToken token = (AbstractOAuth2TokenAuthenticationToken) authentication;
                    String issuer = (String) token.getTokenAttributes().get("iss");
                    TenantDetails tenant = tenantService.getByIssuer(issuer)
                            .orElseThrow(() -> new IllegalArgumentException("Tenant not found for issuer: " + issuer));
                    log.debug("Set TenantContext: {}", tenant.getId());
                    TenantContext.setTenantId(tenant.getId());
                }
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                                   ModelAndView modelAndView) {
                if (TenantContext.getTenantId() != null) {
                    log.debug("Clear TenantContext: {}", TenantContext.getTenantId());
                    TenantContext.clear();
                }
            }
        };
    }

    @Bean
    @ConditionalOnBean(HandlerInterceptorAdapter.class)
    WebMvcConfigurer multiTenantWebMvcConfigurer() {
        return new WebMvcConfigurer() {

            @Autowired
            private HandlerInterceptorAdapter interceptor;

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }

}
