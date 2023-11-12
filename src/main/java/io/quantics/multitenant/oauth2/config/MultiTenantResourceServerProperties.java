package io.quantics.multitenant.oauth2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.annotation.PostConstruct;

/**
 * Multi-tenant resource server properties.
 */
@ConfigurationProperties("spring.security.oauth2.resourceserver.multitenant")
public class MultiTenantResourceServerProperties {

    public static final boolean DEFAULT_ENABLED = false;
    public static final ResolveMode DEFAULT_RESOLVE_MODE = ResolveMode.JWT;

    /**
     * Whether multi-tenant resource server configuration is active.
     */
    private boolean enabled = DEFAULT_ENABLED;

    /**
     * How the tenant is resolved (by JWT or by header).
     */
    private ResolveMode resolveMode = DEFAULT_RESOLVE_MODE;

    /**
     * Options for resolving the tenant by JWT.
     */
    private Jwt jwt = new Jwt();

    /**
     * Options for resolving the tenant by header.
     */
    private Header header = new Header();

    public enum ResolveMode {
        JWT,
        HEADER,
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ResolveMode getResolveMode() {
        return resolveMode;
    }

    public void setResolveMode(ResolveMode resolveMode) {
        this.resolveMode = resolveMode;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public static class Jwt {

        /**
         * Implementation of AbstractJwtGrantedAuthoritiesConverter.
         */
        private String authoritiesConverter;

        @PostConstruct
        void validate() throws ClassNotFoundException {
            if (authoritiesConverter != null
                    && !AbstractJwtGrantedAuthoritiesConverter.class.isAssignableFrom(Class.forName(authoritiesConverter))) {
                throw new IllegalStateException("authoritiesConverter must implement "
                        + AbstractJwtGrantedAuthoritiesConverter.class.getName());
            }
        }

        public String getAuthoritiesConverter() {
            return authoritiesConverter;
        }

        public void setAuthoritiesConverter(String authoritiesConverter) {
            this.authoritiesConverter = authoritiesConverter;
        }

    }

    public static class Header {

        public static final String DEFAULT_HEADER_NAME = "X-TENANT-ID";

        /**
         * Name of the HTTP header which is used for resolving the tenant.
         * Note that this is not suggested for production-grade applications.
         * For production-grade applications, rather resolve the tenant by JWT or another method of your choice.
         */
        private String headerName = DEFAULT_HEADER_NAME;

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

}
