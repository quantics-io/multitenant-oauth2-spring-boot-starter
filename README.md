# Spring Boot Starter for multi-tenant OAuth2 resource servers

This is a starter project for multi-tenant OAuth2 resource servers implemented with Spring. 
The code in this project is based on the samples from the official
[Spring Security documentation](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2resourceserver-multitenancy).

[comment]: <> (TODO add some background info and use cases)

## Installation

Add the dependency to your pom.xml file:

```
<dependency>
    <groupId>io.quantics</groupId>
    <artifactId>multitenant-oauth2-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

The auto-configuration for a multi-tenant OAuth2 resource server can be activated by adding the property
`spring.security.oauth2.resourceserver.multitenant.enabled=true`
to your application properties.

## Configuration

All configuration properties start with the prefix
`spring.security.oauth2.resourceserver.multitenant.*`

Key | Allowed values | Default value
--- | --- | --- 
`enabled` | <ul><li>`true`</li><li>`false`</li></ul> | `false`
`resolve-mode` | <ul><li>`jwt`</li><li>`header`</li></ul> | `jwt`
`jwt.authorities-converter` | Any class extending [`AbstractJwtGrantedAuthoritiesConverter`](src/main/java/io/quantics/multitenant/oauth2/config/AbstractJwtGrantedAuthoritiesConverter.java) | none
`header.header-name` | Any string | X-TENANT-ID

### Resolving the Tenant

#### By Claim

Setting the resolve mode to *JWT* will resolve the tenant by the OAuth2 *iss* claim found in the JWT.


#### By Request Header

Alternatively, the tenant can be resolved by a custom HTTP header by setting the resolve mode to *Header*.

*Note:* This setting is not suggested for production-grade applications, but rather for quick validation that things are working as expected.
