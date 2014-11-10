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
package org.fabric3.binding.ws.metro.provision;

import java.io.Serializable;

/**
 * Encapsulates security configuration for the Metro-based web services binding.
 */
public class SecurityConfiguration implements Serializable {
    private static final long serialVersionUID = 6145747708025591245L;

    private String username;
    private String password;
    private String alias;

    /**
     * Consturctor specifying the username/password for authenitcation.
     *
     * @param username the username
     * @param password the password
     */
    public SecurityConfiguration(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Consturctor specifying the X.509 certificate alias to use for authenitcation.
     *
     * @param alias the alias
     */
    public SecurityConfiguration(String alias) {
        this.alias = alias;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAlias() {
        return alias;
    }
}
