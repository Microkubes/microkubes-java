package com.microkubes.tools.security.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@ConditionalOnProperty(value = {"com.microkubes.security.oauth2", "com.microkubes.security.jwt"})
@EnableOAuth2Sso
public class JWTOAuth2Config {

    @Bean
    public TokenStore getTokenStore(){
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setAccessTokenConverter(new CustomClaimsTokenConverter());
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

}