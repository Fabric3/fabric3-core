/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.jpa.runtime.emf;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.datasource.spi.DataSourceRegistry;

/**
 * @version $Rev$ $Date$
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
        } catch (PersistenceUnitException e) {
            // expected
        }
    }

    protected void setUp() throws Exception {
        DataSourceRegistry registry = EasyMock.createMock(DataSourceRegistry.class);
        EasyMock.expect(registry.getDataSource("EmployeeDS2")).andReturn(EasyMock.createNiceMock(DataSource.class));
        EasyMock.replay(registry);
        MockXMLFactory factory = new MockXMLFactory();
        parser = new PersistenceContextParserImpl(registry, factory);
    }

}
