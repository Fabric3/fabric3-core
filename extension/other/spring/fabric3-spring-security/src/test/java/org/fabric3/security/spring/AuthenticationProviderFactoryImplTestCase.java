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
package org.fabric3.security.spring;

import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.security.spring.config.AuthenticationManagerConfiguration;
import org.fabric3.security.spring.config.JdbcProviderConfiguration;
import org.fabric3.security.spring.factory.AuthenticationProviderFactoryImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

/**
 *
 */
public class AuthenticationProviderFactoryImplTestCase extends TestCase {
    private AuthenticationProviderFactoryImpl factory;

    public void testJdbcProviderCreate() throws Exception {
        AuthenticationManagerConfiguration configuration = new AuthenticationManagerConfiguration();
        JdbcProviderConfiguration jdbcConfiguration = new JdbcProviderConfiguration("test");
        configuration.add(jdbcConfiguration);

        List<AuthenticationProvider> providers = factory.create(configuration);
        assertEquals(1, providers.size());
        AuthenticationProvider provider = providers.get(0);
        assertTrue(provider instanceof DaoAuthenticationProvider);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = new AuthenticationProviderFactoryImpl();
        DataSourceRegistry registry = EasyMock.createMock(DataSourceRegistry.class);
        factory.setRegistry(registry);

    }
}
