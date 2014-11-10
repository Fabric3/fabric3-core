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
package org.fabric3.spi.security;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A token used to authenticate based on a username/password pair.
 */
@XmlRootElement
public class UsernamePasswordToken implements AuthenticationToken<String, String> {
    private String username;
    private String password;

    /**
     * Constructor used for serialization/deserialization with databinding frameworks.
     */
    public UsernamePasswordToken() {
    }

    /**
     * Constructor.
     *
     * @param username the username
     * @param password the password
     */
    public UsernamePasswordToken(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrincipal() {
        return username;
    }

    public String getCredentials() {
        return password;
    }
}
