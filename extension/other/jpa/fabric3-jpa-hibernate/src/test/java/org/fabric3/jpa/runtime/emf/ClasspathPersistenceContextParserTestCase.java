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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.jpa.runtime.emf;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.ContainerException;
import org.fabric3.datasource.spi.DataSourceRegistry;

/**
 *
 */
public class ClasspathPersistenceContextParserTestCase extends TestCase {
    private PersistenceContextParser parser;

    public void testScan() throws Exception {
        List<PersistenceUnitInfo> infos = parser.parse(getClass().getClassLoader());
        assertEquals(2, infos.size());

        PersistenceUnitInfo info1 = infos.get(0);
        assertEquals("2.0", info1.getPersistenceXMLSchemaVersion());
        assertEquals(SharedCacheMode.ALL, info1.getSharedCacheMode());
        assertEquals(ValidationMode.CALLBACK, info1.getValidationMode());
        assertEquals("test", info1.getPersistenceUnitName());
        assertEquals(PersistenceUnitTransactionType.RESOURCE_LOCAL, info1.getTransactionType());
        assertEquals("org.fabric3.TestProvider1", info1.getPersistenceProviderClassName());
        assertEquals(1, info1.getManagedClassNames().size());
        assertEquals("org.fabric3.jpa.Employee", info1.getManagedClassNames().get(0));
        assertEquals(2, info1.getProperties().size());
        assertEquals("val1", info1.getProperties().get("fabric3.prop1"));
        assertEquals("val2", info1.getProperties().get("fabric3.prop2"));
        assertNotNull(info1.getPersistenceUnitRootUrl());

        PersistenceUnitInfo info2 = infos.get(1);
        assertEquals("test2", info2.getPersistenceUnitName());
        assertEquals(PersistenceUnitTransactionType.JTA, info2.getTransactionType());
        assertEquals("org.fabric3.TestProvider2", info2.getPersistenceProviderClassName());
        assertEquals(2, info2.getManagedClassNames().size());
        assertEquals("org.fabric3.jpa.Employee", info2.getManagedClassNames().get(0));
        assertEquals("org.fabric3.jpa.Employee2", info2.getManagedClassNames().get(1));
        assertEquals(2, info2.getProperties().size());
        assertEquals("val1", info2.getProperties().get("fabric3.prop1"));
        assertEquals("val2", info2.getProperties().get("fabric3.prop2"));
        assertNotNull(info2.getPersistenceUnitRootUrl());

    }

    public void testNonExistentPersistenceUnitInfo() throws Exception {
        ClassLoader loader = new ClassLoader() {
            @Override
            public URL getResource(String name) {
                return null;
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                return null;
            }
        };
        try {
            parser.parse(loader);
            fail();
        } catch (ContainerException e) {
            // expected
        }
    }

    protected void setUp() throws Exception {
        DataSourceRegistry registry = EasyMock.createMock(DataSourceRegistry.class);
        EasyMock.expect(registry.getDataSource("EmployeeDS2")).andReturn(EasyMock.createNiceMock(DataSource.class));
        EasyMock.replay(registry);
        parser = new PersistenceContextParserImpl(registry);
    }

}
