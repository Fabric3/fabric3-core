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
 * Configuration for a JDBC-based Spring <code>AuthenticationProvider</code>.
 */
public class JdbcProviderConfiguration extends AuthenticationProviderConfiguration {
    private String dataSourceName;
    private String passwordEncoder;
    private boolean useBase64;

    public JdbcProviderConfiguration(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(String encoder) {
        passwordEncoder = encoder;
    }

    public boolean isUseBase64() {
        return useBase64;
    }

    public void setUseBase64(boolean base64) {
        useBase64 = base64;
    }
}