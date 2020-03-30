package io.quantics.multitenant;

import io.quantics.multitenant.config.web.oauth2.MultiTenantResourceServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MultiTenantResourceServerProperties.class)
public class SpringMultiTenancyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMultiTenancyApplication.class, args);
    }

}
