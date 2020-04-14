# Spring Multi-Tenancy

Multi-tenant capabilities for spring-based OAuth2 resource servers

This project contains 2 different modules:
* [io.quantics.multitenant](src/main/java/io/quantics/multitenant): Spring configuration & auto-configuration for multi-tenant capabilities
* [io.quantics.example.multitenancy](src/main/java/io/quantics/example/multitenancy): Example multi-tenant application utilizing the auto-configuration


## Enabling multi-tenant support

The multi-tenant resource server configuration can be activated by adding the property
`spring.security.oauth2.resourceserver.multitenant.enabled=true`
to the application properties.

## Configuration

All configuration properties start with the prefix
`spring.security.oauth2.resourceserver.multitenant.*`

Key | Allowed values | Default value
--- | --- | --- 
`enabled` | <ul><li>`true`</li><li>`false`</li></ul> | `false`
`resolve-mode` | <ul><li>`jwt`</li><li>`header`</li></ul> | `jwt`
`jwt.authorities-converter` | Any class extending [`AbstractJwtGrantedAuthoritiesConverter`](src/main/java/io/quantics/multitenant/config/oauth2/AbstractJwtGrantedAuthoritiesConverter.java) | none
`header.header-name` | Any string | X-TENANT-ID

### Resolving the Tenant

#### By Claim

Setting the resolve mode to *JWT* will resolve the tenant by the OAuth2 *iss* claim found in the JWT.


#### By Request Header

Alternatively, the tenant can be resolved by a custom HTTP header by setting the resolve mode to *Header*.

*Note:* This setting is not suggested for production-grade applications, but rather for quick validation that things are working as expected.


### Propagating the Tenant

See [Bearer Token Propagation](https://github.com/spring-projects/spring-security/blob/master/docs/manual/src/docs/asciidoc/_includes/servlet/oauth2/oauth2-resourceserver.adoc#oauth2resourceserver-bearertoken-resolver)
in the official Spring Security docs.