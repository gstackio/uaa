/*******************************************************************************
 *     Cloud Foundry
 *     Copyright (c) [2009-2014] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 *******************************************************************************/
package org.cloudfoundry.identity.uaa.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Authentication token which represents a user.
 */
public class UaaAuthentication implements Authentication, Serializable {
    private List<? extends GrantedAuthority> authorities;
    private Object credentials;
    private UaaPrincipal principal;
    private UaaAuthenticationDetails details;
    private boolean authenticated;
    private long authenticatedTime = -1l;
    private long expiresAt = -1l;
    private Set<String> externalGroups;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the
     *            principal represented by this authentication object.
     */
    public UaaAuthentication(UaaPrincipal principal,
                             List<? extends GrantedAuthority> authorities,
                             UaaAuthenticationDetails details) {
        this(principal, null, authorities, details, true, System.currentTimeMillis());
    }

    public UaaAuthentication(UaaPrincipal principal,
                             Object credentials,
                             List<? extends GrantedAuthority> authorities,
                             UaaAuthenticationDetails details,
                             boolean authenticated,
                             long authenticatedTime) {
        this(principal, credentials, authorities, details, authenticated, authenticatedTime, -1);
    }

    @JsonCreator
    public UaaAuthentication(@JsonProperty("principal") UaaPrincipal principal,
                             @JsonProperty("credentials") Object credentials,
                             @JsonProperty("authorities") List<? extends GrantedAuthority> authorities,
                             @JsonProperty("details") UaaAuthenticationDetails details,
                             @JsonProperty("authenticated") boolean authenticated,
                             @JsonProperty(value = "authenticatedTime", defaultValue = "-1") long authenticatedTime,
                             @JsonProperty(value = "expiresAt", defaultValue = "-1") long expiresAt) {
        if (principal == null || authorities == null) {
            throw new IllegalArgumentException("principal and authorities must not be null");
        }
        this.principal = principal;
        this.authorities = authorities;
        this.details = details;
        this.credentials = credentials;
        this.authenticated = authenticated;
        this.authenticatedTime = authenticatedTime <= 0 ? -1 : authenticatedTime;
        this.expiresAt = expiresAt <= 0 ? -1 : expiresAt;
    }

    public UaaAuthentication(UaaPrincipal uaaPrincipal,
                             Object credentials,
                             List<? extends GrantedAuthority> uaaAuthorityList,
                             Set<String> externalGroups,
                             UaaAuthenticationDetails details,
                             boolean authenticated,
                             long authenticatedTime,
                             long expiresAt) {
        this(uaaPrincipal, credentials, uaaAuthorityList, details, authenticated, authenticatedTime, expiresAt);
        this.externalGroups = externalGroups;
    }

    public long getAuthenticatedTime() {
        return authenticatedTime;
    }

    @Override
    @JsonIgnore
    public String getName() {
        // Should we return the ID for the principal name? (No, because the
        // UaaUserDatabase retrieves users by name.)
        return principal.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public UaaPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated && (expiresAt > 0 ? expiresAt > System.currentTimeMillis() : true);
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        authenticated = isAuthenticated;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UaaAuthentication that = (UaaAuthentication) o;

        if (!authorities.equals(that.authorities)) {
            return false;
        }
        if (!principal.equals(that.principal)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = authorities.hashCode();
        result = 31 * result + principal.hashCode();
        return result;
    }

    public Set<String> getExternalGroups() {
        return externalGroups;
    }

    public void setExternalGroups(Set<String> externalGroups) {
        this.externalGroups = externalGroups;
    }
}
