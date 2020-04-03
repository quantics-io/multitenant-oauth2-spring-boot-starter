# Spring Multi-Tenancy

Multi-tenant capabilities for spring-based OAuth2 resource servers


## Configuration

The multi-tenant resource server configuration can be activated by adding the property
`spring.security.oauth2.resourceserver.multitenant.enabled=true`
to the application properties.


## Implementation

### Resolving the Tenant

#### By Claim

#### By Request Header

#### By Domain


### Propagating the Tenant

See [Bearer Token Propagation](https://github.com/spring-projects/spring-security/blob/master/docs/manual/src/docs/asciidoc/_includes/servlet/oauth2/oauth2-resourceserver.adoc#oauth2resourceserver-bearertoken-resolver)
in the official Spring Security docs.