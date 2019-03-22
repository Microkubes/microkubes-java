package com.microkubes.tools.gateway.spring;

import com.microkubes.tools.gateway.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "com.microkubes.gateway", name="gateway-url")
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

    @Bean
    public ServiceRegistry getServiceRegistry() {
        return new KongServiceRegistry(apiGatewayURL);
    }

    @Bean
    public ServiceInfo getServiceInfo() throws ValidationException {
        ServiceInfo.ServiceInfoBuilder serviceInfo =  ServiceInfo
                .NewService(serviceName)
                .host(serviceHost)
                .port(servicePort);

        for( String path: servicePaths) {
            serviceInfo.addPath(path);
        }

        return  serviceInfo.getServiceInfo();
    }
}
