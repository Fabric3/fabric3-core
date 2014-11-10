/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.security.spring.config;

/**
 * Configuration for an LDAP-based Spring <code>AuthenticationProvider</code>.
 */
public class LdapProviderConfiguration extends AuthenticationProviderConfiguration {
    private String password;
    private String[] dnPattern;
    private String userSearchBase;
    private String userSearchFilter;
    private String groupSearchBase;
    private String groupSearchFilter = "uniqueMember={0}";
    private String groupRoleAttribute = "cn";

    public String getPassword() {
        return password;
    }

    public String[] getDnPattern() {
        return dnPattern;
    }

    public void setDnPattern(String[] dnPattern) {
        this.dnPattern = dnPattern;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public void setUserSearchBase(String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public void setGroupSearchBase(String base) {
        groupSearchBase = base;

    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(String filter) {
        groupSearchFilter = filter;
    }

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    public void setGroupRoleAttribute(String attribute) {
        this.groupRoleAttribute = attribute;
    }
}