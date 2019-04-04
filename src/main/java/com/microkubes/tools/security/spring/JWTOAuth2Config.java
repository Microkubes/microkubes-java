package com.microkubes.tools.security.spring;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


@Configuration
@ConditionalOnProperty(value = {"com.microkubes.security.oauth2_jwt"})
public class JWTOAuth2Config {

    @Value("com.microkubes.security.private_key.path")
    private String privateKeyPath;

    @Bean
    public TokenStore getTokenStore() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setAccessTokenConverter(new CustomClaimsTokenConverter());
        jwtAccessTokenConverter.setKeyPair(loadKeyPair());
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    public KeyPair loadKeyPair() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PemReader reader = new PemReader(new FileReader(privateKeyPath));
        PKCS8EncodedKeySpec privateKey = new PKCS8EncodedKeySpec(reader.readPemObject().getContent());

        KeyFactory kf = KeyFactory.getInstance("RSA");

        return  new KeyPair(kf.generatePublic(privateKey), kf.generatePrivate(privateKey));
    }

}
