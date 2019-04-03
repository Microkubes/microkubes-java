package com.microkubes.tools.security.spring;

import com.microkubes.tools.security.Auth;

/**
 * Defines a holder for the Auth object, which in turns holds the authentication/authorization data
 * for Microkubes security system.
 */
public interface AuthenticationHolder {

    /**
     * Get the current Auth object.
     * @return the current decoded Auth object (possibly null).
     */
    Auth getAuth();

}
