package io.quantics.multitenant.config.web;

import io.quantics.multitenant.config.web.interceptor.MultiTenantHeaderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Configuration
public class MultiTenantWebMvcConfigurer implements WebMvcConfigurer {

    private final HandlerInterceptorAdapter interceptor;

    @Autowired
    public MultiTenantWebMvcConfigurer(MultiTenantHeaderInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
