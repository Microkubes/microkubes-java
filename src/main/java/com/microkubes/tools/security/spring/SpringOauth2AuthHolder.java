package com.microkubes.tools.security.spring;

import com.microkubes.tools.security.Auth;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

/**
 * Implements AuthenticationHolder and enhances the OAuth2Authentication for Spring Security.
 */
public class SpringOauth2AuthHolder extends OAuth2Authentication implements AuthenticationHolder {

    private Auth auth;

    /**
     * Construct an OAuth 2 authentication. Since some grant types don't require user authentication, the user
     * authentication may be null.
     *
     * @param storedRequest      The authorization request (must not be null).
     * @param userAuthentication The user authentication (possibly null).
     * @param auth               The Auth object constructed from Microkubes security (possibly null).
     */
    public SpringOauth2AuthHolder(OAuth2Request storedRequest, Authentication userAuthentication, Auth auth) {
        super(storedRequest, userAuthentication);
        this.auth = auth;
    }

    @Override
    public Auth getAuth() {
        return auth;
    }
}
