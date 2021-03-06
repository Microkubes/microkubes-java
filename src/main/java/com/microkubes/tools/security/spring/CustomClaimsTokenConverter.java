package com.microkubes.tools.security.spring;

import com.microkubes.tools.security.Auth;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * TokenConverter for Microkubes custom claims in the JWT.
 * Extends the basic Spring functionality and adds support and integration with
 * Microkubes provided security and JWT based authentication and authorization.
 */
public class CustomClaimsTokenConverter extends DefaultAccessTokenConverter {

    private String scopeAttribute = SCOPE;
    private UserAuthenticationConverter userTokenConverter = new DefaultUserAuthenticationConverter();
    private boolean includeGrantType;
    private String clientIdAttribute = CLIENT_ID;

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ORGANIZATIONS = "organizations";
    public static final String CLAIM_NAMESPACES = "namespaces";

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        Map<String, String> parameters = new HashMap<String, String>();
        Set<String> scope = extractScope(map);
        Authentication user = getUserTokenConverter().extractAuthentication(enhanceClaimsMap(map));
        String clientId = (String) map.get(getClientIdAttribute());
        parameters.put(getClientIdAttribute(), clientId);
        if (isIncludeGrantType() && map.containsKey(GRANT_TYPE)) {
            parameters.put(GRANT_TYPE, (String) map.get(GRANT_TYPE));
        }
        Set<String> resourceIds = new LinkedHashSet<String>(map.containsKey(AUD) ? getAudience(map)
                : Collections.<String>emptySet());
        OAuth2Request request = new OAuth2Request(parameters, clientId, extractAuthorities(map), true, scope, resourceIds, null, null,
                null);
        return new SpringOauth2AuthHolder(request, user, extractAuth(map));
    }

    /**
     * Extracts the authorities provided by Spring's standard claims and enhances them with Microkubes roles.
     * @param map the claims map.
     * @return collection of aggregated authorities.
     */
    protected Collection<? extends GrantedAuthority> extractAuthorities(Map<String, ?> map){
        Set<GrantedAuthority> allAuthorities = new HashSet<>();
        if (map.containsKey(AUTHORITIES)){
            allAuthorities.addAll(AuthorityUtils.createAuthorityList(((Collection<String>)map.get(AUTHORITIES)).toArray(new String[0])));
        }
        if (map.containsKey(CLAIM_ROLES)){
            allAuthorities.addAll(AuthorityUtils.commaSeparatedStringToAuthorityList((String)map.get(CLAIM_ROLES)));
        }

        return  allAuthorities;
    }


    /**
     * Extracts the {@link Auth} object from the provided JWT claims map.
     * @param map the claims map
     * @return extracted {@link Auth} object.
     */
    protected Auth extractAuth(Map<String, ?> map) {
        String username = (String) map.get(CLAIM_USERNAME);
        String userId = (String) map.get(CLAIM_USER_ID);
        List<String> roles = listFormCommaSeparatedString((String)map.get(CLAIM_ROLES));
        List<String> organizations = listFormCommaSeparatedString((String)map.get(CLAIM_ORGANIZATIONS));
        List<String> namespaces = listFormCommaSeparatedString((String)map.get(CLAIM_NAMESPACES));

        return new Auth(username, userId, roles, organizations, namespaces);
    }

    /**
     * Helper to tokenize comma-separated string into a list of values.
     * @param value the comma-separated string.
     * @return list of extracted values.
     */
    protected List<String> listFormCommaSeparatedString(String value) {
        if(value == null){
            return  null;
        }
        return Arrays.asList(StringUtils.tokenizeToStringArray(value, ","));
    }

    /**
     * Enhances the given claims map from Spring's security with Microkubes claims.
     * Some of the properties, like "username" and "roles" are re-mapped to Spring's
     * username and authorities custom claims.
     *
     * The input map shall not be modified, instead a new map is populated.
     *
     * @param map the claims map. This map will not be modified after calling this function.
     * @return enhanced map containing Microkubes claims.
     */
    protected Map<String, ?> enhanceClaimsMap(Map<String, ?> map) {
        Map<String, Object> enhanced = new HashMap<>();
        enhanced.putAll(map);
        if(map.containsKey(CLAIM_USERNAME)){
            enhanced.put(UserAuthenticationConverter.USERNAME, map.get(CLAIM_USERNAME));
        }
        if(map.containsKey(CLAIM_ROLES)){
            enhanced.put(UserAuthenticationConverter.AUTHORITIES, map.get(CLAIM_ROLES));
        }
        return enhanced;
    }

    private Set<String> extractScope(Map<String, ?> map) {
        Set<String> scope = Collections.emptySet();
        if (map.containsKey(scopeAttribute)) {
            Object scopeObj = map.get(scopeAttribute);
            if (String.class.isInstance(scopeObj)) {
                scope = new LinkedHashSet<String>(Arrays.asList(String.class.cast(scopeObj).split(" ")));
            } else if (Collection.class.isAssignableFrom(scopeObj.getClass())) {
                @SuppressWarnings("unchecked")
                Collection<String> scopeColl = (Collection<String>) scopeObj;
                scope = new LinkedHashSet<String>(scopeColl);	// Preserve ordering
            }
        }
        return scope;
    }

    private Collection<String> getAudience(Map<String, ?> map) {
        Object auds = map.get(AUD);
        if (auds instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> result = (Collection<String>) auds;
            return result;
        }
        return Collections.singleton((String)auds);
    }

    public String getScopeAttribute() {
        return scopeAttribute;
    }

    @Override
    public void setScopeAttribute(String scopeAttribute) {
        this.scopeAttribute = scopeAttribute;
    }

    public UserAuthenticationConverter getUserTokenConverter() {
        return userTokenConverter;
    }

    @Override
    public void setUserTokenConverter(UserAuthenticationConverter userTokenConverter) {
        this.userTokenConverter = userTokenConverter;
    }

    public boolean isIncludeGrantType() {
        return includeGrantType;
    }

    @Override
    public void setIncludeGrantType(boolean includeGrantType) {
        this.includeGrantType = includeGrantType;
    }

    public String getClientIdAttribute() {
        return clientIdAttribute;
    }

    @Override
    public void setClientIdAttribute(String clientIdAttribute) {
        this.clientIdAttribute = clientIdAttribute;
    }
}
