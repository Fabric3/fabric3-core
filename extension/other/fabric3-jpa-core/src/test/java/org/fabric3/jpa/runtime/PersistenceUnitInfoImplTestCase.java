/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.jpa.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.w3c.dom.Document;

public class PersistenceUnitInfoImplTestCase extends TestCase {

    private static final String PERSISTENCE_PROVIDER = "PERSISTENCE_PROVIDER";
    private static final String UNIT_NAME = "UNIT_NAME";
    private static final String TRANS_TYPE = "TRANS_TYPE";
    private static final String DS_NAME = "DS_NAME";

    public void testFirstOfMultiple() throws Exception {

        URL persistenceUnitUrl = getPersistenceUnitUrl();

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document persistenceDom = db.parse(persistenceUnitUrl.openStream());

        final String expectedUnitName = "test";
        Map<String, String> expectedSimpleValues = new HashMap<String, String>();
        expectedSimpleValues.put(UNIT_NAME, expectedUnitName);
        expectedSimpleValues.put(PERSISTENCE_PROVIDER, "org.apache.openjpa.persistence.PersistenceProviderImpl");
        expectedSimpleValues.put(TRANS_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL.toString());
        expectedSimpleValues.put(DS_NAME, null);

        HashSet<String> expectedEntityClasses = new HashSet<String>();
        expectedEntityClasses.add("org.fabric3.jpa.Employee");

        Properties expectedProperties = new Properties();
        expectedProperties.put("openjpa.ConnectionURL", "jdbc:hsqldb:tutorial_database");
        expectedProperties.put("openjpa.ConnectionDriverName", "org.hsqldb.jdbcDriver");
        expectedProperties.put("openjpa.ConnectionUserName", "sa");
        expectedProperties.put("openjpa.ConnectionPassword", "");
        expectedProperties.put("openjpa.Log", "DefaultLevel=WARN, Tool=INFO");

        F3PersistenceUnitInfo matchedUnit =
                F3PersistenceUnitInfo.getInstance(expectedUnitName, persistenceDom, getClass().getClassLoader(), persistenceUnitUrl);

        assertState(matchedUnit, expectedSimpleValues, expectedEntityClasses, expectedProperties);
    }

    public void testLastOfMultiple() throws Exception {

        URL persistenceUnitUrl = getPersistenceUnitUrl();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document persistenceDom = db.parse(persistenceUnitUrl.openStream());

        final String expectedUnitName = "testThree";
        Map<String, String> expectedSimpleValues = new HashMap<String, String>();
        expectedSimpleValues.put(UNIT_NAME, expectedUnitName);
        expectedSimpleValues.put(PERSISTENCE_PROVIDER, "org.test.ProviderNameThree");
        expectedSimpleValues.put(TRANS_TYPE, PersistenceUnitTransactionType.JTA.toString());
        expectedSimpleValues.put(DS_NAME, "EmployeeDSThree");

        HashSet<String> expectedEntityClasses = new HashSet<String>();
        expectedEntityClasses.add("org.fabric3.jpa.model.Employee");
        expectedEntityClasses.add("org.fabric3.jpa.model.Employee2");
        expectedEntityClasses.add("org.fabric3.jpa.model.Employee3");
        expectedEntityClasses.add("org.fabric3.jpa.model.Employee4");

        Properties expectedProperties = new Properties();
        expectedProperties.put("hibernate.dialect", "org.hibernate.test.dialect.Three");
        expectedProperties.put("hibernate.transaction.manager_lookup_class", "org.fabric3.jpa.hibernate.F3HibernateTransactionManagerLookupThree");
        expectedProperties.put("hibernate.hbm2ddl.auto", "create-drop-three");

        F3PersistenceUnitInfo matchedUnit =
                F3PersistenceUnitInfo.getInstance(expectedUnitName, persistenceDom, getClass().getClassLoader(), persistenceUnitUrl);

        assertState(matchedUnit, expectedSimpleValues, expectedEntityClasses, expectedProperties);
    }

    public void testMiddleOfMultiple() throws Exception {

        URL persistenceUnitUrl = getPersistenceUnitUrl();

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document persistenceDom = db.parse(persistenceUnitUrl.openStream());

        final String expectedUnitName = "testTwo";
        Map<String, String> expectedSimpleValues = new HashMap<String, String>();
        expectedSimpleValues.put(UNIT_NAME, expectedUnitName);
        expectedSimpleValues.put(PERSISTENCE_PROVIDER, "org.test.ProviderNameTwo");
        expectedSimpleValues.put(TRANS_TYPE, PersistenceUnitTransactionType.JTA.toString());
        expectedSimpleValues.put(DS_NAME, "EmployeeDSTwo");

        HashSet<String> expectedEntityClasses = new HashSet<String>();
        expectedEntityClasses.add("org.fabric3.jpa.model.Employee");
        expectedEntityClasses.add("org.fabric3.jpa.model.Employee2");

        Properties expectedProperties = new Properties();
        expectedProperties.put("hibernate.dialect", "org.hibernate.test.dialect.Two");
        expectedProperties.put("hibernate.transaction.manager_lookup_class", "org.fabric3.jpa.hibernate.F3HibernateTransactionManagerLookupTwo");
        expectedProperties.put("hibernate.hbm2ddl.auto", "create-drop-two");

        F3PersistenceUnitInfo matchedUnit =
                F3PersistenceUnitInfo.getInstance(expectedUnitName, persistenceDom, getClass().getClassLoader(), persistenceUnitUrl);

        assertState(matchedUnit, expectedSimpleValues, expectedEntityClasses, expectedProperties);
    }


    private void assertState(F3PersistenceUnitInfo matchedUnit,
                             Map<String, String> expectedResults,
                             HashSet<String> expectedEntityClasses,
                             Properties expectedProperties) {
        assertEquals(expectedResults.get(UNIT_NAME), matchedUnit.getPersistenceUnitName());
        assertEquals(expectedResults.get(PERSISTENCE_PROVIDER), matchedUnit.getPersistenceProviderClassName());
        assertEquals(expectedResults.get(TRANS_TYPE), matchedUnit.getTransactionType().toString());
        assertEquals(expectedResults.get(DS_NAME), matchedUnit.getDataSourceName());

        //Order insensitive comparison of the entity class names
        HashSet<String> actualEntityClasses = new HashSet<String>(matchedUnit.getManagedClassNames());
        assertTrue(expectedEntityClasses.equals(actualEntityClasses));

        assertTrue(expectedProperties.equals(matchedUnit.getProperties()));
    }

    private URL getPersistenceUnitUrl() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        Enumeration<URL> persistenceUnitUrls = classLoader.getResources("META-INF/persistence.xml");

        //One and only one persistence unit resource match is expected for the tests
        assertTrue(persistenceUnitUrls.hasMoreElements());
        URL persistenceUnitUrl = persistenceUnitUrls.nextElement();
        assertFalse(persistenceUnitUrls.hasMoreElements());
        return persistenceUnitUrl;
    }
}
