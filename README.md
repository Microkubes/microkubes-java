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

## Use the latest version from Github

To use the latest version, first clone this repository locally:

```bash
git clone https://github.com/Microkubes/microkubes-java.git
```

Make sure you have [Gradle](https://gradle.org/) installed, then:

```bash
cd microkubes-java
./gradlew build -x sign -x uploadArchives
```

Then install the latest version to your local Maven repository:

```bash
./gradlew publishToMavenLocal
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

* `com.microkubes.gateway.adapter` the version of adapter used for API Gateway. Currently supported: kong-v0, kong-v2. Default: [kong-v0]
* `com.microkubes.gateway.gateway-url` the URL of the admin port of Kong in the platform deployment. Usually set to `http://kong:8001`. 
Note that this property triggers the auto-configuration process and must be present for the service to auto-register on the platform.
* `com.microkubes.service.name` -  the name of the service
* `com.microkubes.service.host` - the virtual host (domain name) of the service as it will be on the platform itself. This is the
internal domain of the service as visible to the Consul DNS.
* `com.microkubes.service.port` - the port on which the service listens to.
* `com.microkubes.service.paths` - routing paths (separated by comma) for the incoming requests to be proxied to this microservice.

Additional service configuration options:

* `com.microkubes.service.preserve-host` - Whether to pass the `Host` header to the upstream API service. Default `false`.
* `com.microkubes.service.retries` - The number of retries to execute upon failure to proxy. Default `5`.
* `com.microkubes.service.strip-uri` - When matching an API via one of the `uris` prefixes, strip that matching prefix 
from the upstream URI to be requested.  Default `true`.
* `com.microkubes.service.upstream-connect-timeout` - The timeout in milliseconds for establishing a connection between
the API Gateway and the service. Default `60000`.
* `com.microkubes.service.upstream-read-timeout` - he timeout in milliseconds between two successive read operations for
transmitting a request to your the service. Default `60000`.
* `com.microkubes.service.upstream-send-timeout` - The timeout in milliseconds between two successive write operations
for transmitting a request to the service. Default `60000`.
* `com.microkubes.service.https-only` - To be enabled if you wish to only serve your API through HTTPS, on the appropriate
port (8443 by default). Default `false`.
* `com.microkubes.service.http-if-terminated` - Tell the API Gateway to consider the `X-Forwarded-Proto` header when enforcing
HTTPS only traffic. Default `false`.


## Adding plugins to the service definition

If you wan to enable specific plugins (currently supported for Kong API Gateway), you can do so by adding
the specific properties in the configuration file, or via ENV variables.

Plugins configuration is loaded from the properties basd on a property name prefix. The general pattern is:

```
com.microkubes.service.plugins.<plugin_name>.<plugin_prop>
```
or as an ENV variable:

```
com_microkubes_service_plugins_<plugin_name>_<plugin_prop>
```

where:
* `plugin_name` is the name of the plugin to be enabled, for example: `cors`.
* `plugin_prop` are the specific propeties for the plugin. Usually something like: `config.max_age`, `config.headers` etc.

Here is an example of configuring the `cors` plugin in a `.properties` file:

```properties
com.microkubes.service.plugins.cors.config.methods=GET,PUT,POST,DELETE
com.microkubes.service.plugins.cors.config.origins=*
com.microkubes.service.plugins.cors.config.headers=Authorization,X-Auth-Token
com.microkubes.service.plugins.cors.config.credentials=true
com.microkubes.service.plugins.cors.config.max_age=3600
```
This would enable and configure the `cors` plugin.

The same configuration can be done via environment variables:

```shell
com_microkubes_service_plugins_cors_config_methods=GET,PUT,POST,DELETE
com_microkubes_service_plugins_cors_config_origins=*
com_microkubes_service_plugins_cors_config_headers=Authorization,X-Auth-Token
com_microkubes_service_plugins_cors_config_credentials=true
com_microkubes_service_plugins_cors_config_max__age=3600
```

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