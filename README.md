Microkubes Java Library
=======================

Tools for [Microkubes](https://microkubes.com) in Java.


# Installation

The library can be installed from Maven central:

Maven:

```
<dependency>
    <groupId>com.microkubes</groupId>
    <artifactId>tools</artifactId>
    <version>0.1.0.RELEASE</version>
</dependency>
```

Gradle:

```
compile group: 'com.microkubes', name: 'tools', version: '0.1.0.RELEASE'
```

# Service API Gateway Integration

The library provides an API for connecting and aut-registering with Kong API Gateway (default for Microkubes).

When using it with Spring, you can import the provided configuration class that will enable the aut-register process
based on configuration properties:

```java
@EnableAutoConfiguration()
@SpringBootApplication
@Import({AutoRegister.class})
@PropertySource("classpath:config.properties")
public class Application {

    public static void main(String [] args){
        SpringApplication.run(Application.class, args);
    }
}
```

and the config properties should look like this:

```
com.microkubes.gateway.gateway-url = http://kong:8001
com.microkubes.service.name = todo
com.microkubes.service.host = todo.service.consul
com.microkubes.service.port = 8080
com.microkubes.service.paths = /todo
```

Configuration options:

* `com.microkubes.gateway.gateway-url` the URL of the admin port of Kong in the platform deployment. Usually set to `http://kong:8001`. 
Note that this property triggers the auto-configuration process and must be present for the service to auto-register on the platform.
* `com.microkubes.service.name` -  the name of the service
* `com.microkubes.service.host` - the virtual host (domain name) of the service as it will be on the platform itself. This is the
internal domain of the service as visible to the Consul DNS.
* `com.microkubes.service.port` - the port on which the service listens to.
* `com.microkubes.service.paths` - routing paths (separated by comma) for the incoming requests to be proxied to this microservice.



# Security Integration

The library offers seamless integration with Microkubes security with Spring Security and SpringBoot enabled microservices.

## JWT and OAuth2

To enable JWT and ResourceServer you can use the `@EnableSecurity` annotation on the `SpringBoot` application (configuration) class:

```java
@EnableAutoConfiguration
@SpringBootApplication
@PropertySource("classpath:config.properties")
@EnableSecurity
@EnableResourceServer
public class Application {

    public static void main(String [] args){
        SpringApplication.run(Application.class, args);
    }
}
```

You need to set up some configuration properties as well. In `config.properties`:

```
# enable JWT/OAuth2 on the REST API
com.microkubes.security.oauth2_jwt=enable

# Set the path to the private key (must be PEM encoded PKCS8 private key).
com.microkubes.security.private_key.path=keys/system.pem

# Set the path to the public key (X.509 encoded file).
com.microkubes.security.public_key.path=keys/system.pub
```

Note that this property `com.microkubes.security.oauth2_jwt=enable` actually triggers the setup and integration with
Microkubes security.

Don't forget to add `@EnableResourceServer` or `@EnableOauth2Sso` to setup the security chain.