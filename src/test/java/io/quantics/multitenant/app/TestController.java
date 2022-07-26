package io.quantics.multitenant.app;

import io.quantics.multitenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class TestController {

    @GetMapping
    public String get() {
        String tenantId = TenantContext.getTenantId();
        StringBuilder result = new StringBuilder("Hello World from " + tenantId + "!");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().size() > 0) {
            result.append("\n\n").append("Granted authorities:").append("\n");
            authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .forEach(a -> result.append("- ").append(a).append("\n"));
        }

        return result.toString();
    }

}
