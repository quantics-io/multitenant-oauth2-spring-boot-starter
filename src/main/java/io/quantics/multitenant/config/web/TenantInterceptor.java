package io.quantics.multitenant.config.web;

import io.quantics.multitenant.tenant.service.TenantService;
import io.quantics.multitenant.util.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class TenantInterceptor extends HandlerInterceptorAdapter {

    private final TenantService tenantService;

    @Autowired
    public TenantInterceptor(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader("X-TENANT-ID");
        log.debug("Set TenantContext: {}", tenantId);
        TenantContext.setTenantId(tenantId);
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
}