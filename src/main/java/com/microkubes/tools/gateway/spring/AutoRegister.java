package com.microkubes.tools.gateway.spring;

import com.microkubes.tools.gateway.ServiceInfo;
import com.microkubes.tools.gateway.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(prefix = "com.microkubes.gateway", name="gateway-url")
@Import(ServiceRegistryConfig.class)
public class AutoRegister {

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private ServiceInfo serviceInfo;

    @PostConstruct
    public void init(){
        serviceRegistry.register(serviceInfo);
    }
}
