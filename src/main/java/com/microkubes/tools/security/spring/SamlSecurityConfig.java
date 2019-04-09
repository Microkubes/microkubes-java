package com.microkubes.tools.security.spring;

import org.apache.commons.httpclient.HttpClient;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@Configuration
public class SamlSecurityConfig {

    @Value("${com.microkubes.security.saml.metadataUrl}")
    private String samlIdPMetadataUrl;

    @Autowired
    private Timer timer;

    @Bean
    public CachingMetadataManager getMetadataManager() throws MetadataProviderException {
        List<MetadataProvider> metadata = new ArrayList<>();
        metadata.add(new HTTPMetadataProvider(timer, new HttpClient(), samlIdPMetadataUrl));
        return new CachingMetadataManager(metadata);
    }
    
}
