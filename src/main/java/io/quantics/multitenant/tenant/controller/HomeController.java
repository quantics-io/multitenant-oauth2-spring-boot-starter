package io.quantics.multitenant.tenant.controller;

import io.quantics.multitenant.util.TenantContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String get() {
        return "Hello World from " + TenantContext.getTenantId() + "! \n"
                + "\n"
                + "Granted authorities: \n"
                + SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining("\n- ", "- ", ""));
    }
}
