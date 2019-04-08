package com.microkubes.tools.security;

import java.util.List;
import java.util.Objects;

/**
 * Authorization object.
 * Holds the data for the authenticated and authorized user on the platform.
 * The data is extracted usually from the JWT by the security chain mechanism.
 */
public class Auth {
    private String email;
    private String userId;
    private List<String> roles;
    private List<String> organizations;
    private List<String> namespaces;

    /**
     * Creates new Authorization from the given user data.
     *
     * @param email         user email.
     * @param userId        user ID.
     * @param roles         list of granted user roles.
     * @param organizations list of organizations the user is member of.
     * @param namespaces    list of namespaces the user belongs to.
     */
    public Auth(String email, String userId, List<String> roles, List<String> organizations, List<String> namespaces) {
        this.email = email;
        this.userId = userId;
        this.roles = roles;
        this.organizations = organizations;
        this.namespaces = namespaces;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public List<String> getNamespaces() {
        return namespaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Auth auth = (Auth) o;
        return userId.equals(auth.userId) &&
                Objects.equals(roles, auth.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roles);
    }
}
