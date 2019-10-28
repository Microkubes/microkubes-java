package com.microkubes.tools.gateway.spring;

import com.microkubes.tools.gateway.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = "com.microkubes.tools.gateway.spring")
@ConditionalOnProperty(prefix = "com.microkubes.gateway", name = "gateway-url")
public class ServiceRegistryConfig {

    @Value("${com.microkubes.gateway.gateway-url}")
    private String apiGatewayURL;

    @Value("${com.microkubes.service.name}")
    private String serviceName;
    @Value("${com.microkubes.service.host}")
    private String serviceHost;
    @Value("${com.microkubes.service.port}")
    private Integer servicePort;
    @Value("${com.microkubes.service.paths}")
    private String[] servicePaths;

    // Additional service API configuration properties
    @Value("${com.microkubes.service.preserve-host:false}")
    private Boolean preserveHost;
    @Value("${com.microkubes.service.retries:5}")
    private Integer retries;
    @Value("${com.microkubes.service.strip-uri:true}")
    private Boolean stripUri;
    @Value("${com.microkubes.service.upstream-connect-timeout:60000}")
    private Integer upstreamConnectTimeout;
    @Value("${com.microkubes.service.upstream-read-timeout:60000}")
    private Integer upstreamReadTimeout;
    @Value("${com.microkubes.service.upstream-send-timeout:60000}")
    private Integer upstreamSendTimeout;
    @Value("${com.microkubes.service.https-only:false}")
    private Boolean httpsOnly;
    @Value("${com.microkubes.service.http-if-terminated:false}")
    private Boolean httpIfTerminated;

    @Autowired
    private ServicePluginsConfig servicePlugins;


    @Bean
    public ServiceRegistry getServiceRegistry() {
        return new KongServiceRegistry(apiGatewayURL);
    }

    @Bean
    public ServiceInfo getServiceInfo() throws ValidationException {
        ServiceInfo.ServiceInfoBuilder serviceInfo = ServiceInfo
                .NewService(serviceName)
                .host(serviceHost)
                .port(servicePort);

        for (String path : servicePaths) {
            serviceInfo.addPath(path);
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("preserve_host", preserveHost);
        properties.put("retries", retries);
        properties.put("strip_uri", stripUri);
        properties.put("upstream_connect_timeout", upstreamConnectTimeout);
        properties.put("upstream_read_timeout", upstreamReadTimeout);
        properties.put("upstream_send_timeout", upstreamSendTimeout);
        properties.put("https_only", httpsOnly);
        properties.put("http_if_terminated", httpIfTerminated);

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (entry.getValue() != null) {
                serviceInfo.setProperty(entry.getKey(), entry.getValue());
            }
        }
        for (ServicePlugin plugin : servicePlugins.getPlugins().values()) {
            serviceInfo.addPlugin(plugin);
        }

        return serviceInfo.getServiceInfo();
    }
}
