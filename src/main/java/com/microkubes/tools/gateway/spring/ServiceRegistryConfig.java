package com.microkubes.tools.gateway.spring;

import com.microkubes.tools.gateway.KongServiceRegistry;
import com.microkubes.tools.gateway.ServiceInfo;
import com.microkubes.tools.gateway.ServiceRegistry;
import com.microkubes.tools.gateway.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
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

    @Value("${com.microkubes.service.preserve_host:false}")
    private Boolean preserveHost;
    @Value("${com.microkubes.service.retries:5}")
    private Integer retries;
    @Value("${com.microkubes.service.strip_uri:true}")
    private Boolean stripUri;
    @Value("${com.microkubes.service.upstream_connect_timeout:60000}")
    private Integer upstreamConnectTimeout;
    @Value("${com.microkubes.service.upstream_read_timeout:60000}")
    private Integer upstreamReadTimeout;
    @Value("${com.microkubes.service.upstream_send_timeout:60000}")
    private Integer upstreamSendTimeout;
    @Value("${com.microkubes.service.https_only:false}")
    private Integer httpsOnly;
    @Value("${com.microkubes.service.http_if_terminated:false}")
    private Integer httpIfTerminated;


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

        return serviceInfo.getServiceInfo();
    }
}
