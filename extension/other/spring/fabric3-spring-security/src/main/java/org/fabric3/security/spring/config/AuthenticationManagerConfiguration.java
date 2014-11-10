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

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for instantiating a Spring <code>AuthenticationManager</code>
 */
public class AuthenticationManagerConfiguration {
    private String alias;
    private boolean eraseCredentials;
    private LdapServerConfiguration ldapServerConfiguration;

    private List<AuthenticationProviderConfiguration> configurations = new ArrayList<>();

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isEraseCredentials() {
        return eraseCredentials;
    }

    public void setEraseCredentials(boolean eraseCredentials) {
        this.eraseCredentials = eraseCredentials;
    }

    public void add(AuthenticationProviderConfiguration configuration) {
        configurations.add(configuration);
    }

    public List<AuthenticationProviderConfiguration> getProviderConfigurations() {
        return configurations;
    }

    public LdapServerConfiguration getLdapServerConfiguration() {
        return ldapServerConfiguration;
    }

    public void setLdapServerConfiguration(LdapServerConfiguration configuration) {
        this.ldapServerConfiguration = configuration;
    }
}
