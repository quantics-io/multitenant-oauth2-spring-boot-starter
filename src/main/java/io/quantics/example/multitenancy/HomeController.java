package io.quantics.example.multitenancy;

import io.quantics.multitenant.util.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String get() {
        StringBuilder result = new StringBuilder();
        if (TenantContext.getTenantId() == null) {
            result.append("Hello World!");
        } else {
            result.append("Hello World from ")
                    .append(TenantContext.getTenantId())
                    .append("!");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().size() > 0) {
            result.append("\n\n")
                    .append("Granted authorities:").append("\n");
            authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .forEach(a -> result.append("- ").append(a).append("\n"));
        }

        return result.toString();
    }

}
