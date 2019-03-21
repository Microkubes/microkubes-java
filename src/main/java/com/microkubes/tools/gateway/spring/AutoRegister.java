package com.microkubes.tools.gateway.spring;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AutoRegister {

    @PostConstruct
    public void init(){
        System.out.println("Microkubes AutoRegistration with KONG API Gateway.");
    }
}
