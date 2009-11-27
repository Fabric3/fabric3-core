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

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public class ClasspathPersistenceUnitScanner implements PersistenceUnitScanner {

    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    private Map<String, PersistenceUnitInfo> persistenceUnitInfos = new HashMap<String, PersistenceUnitInfo>();

    public PersistenceUnitInfo getPersistenceUnitInfo(String unitName, ClassLoader classLoader) {

        synchronized (persistenceUnitInfos) {

            if (persistenceUnitInfos.containsKey(unitName)) {
                return persistenceUnitInfos.get(unitName);
            }

            try {

                DocumentBuilder db = dbf.newDocumentBuilder();

                Enumeration<URL> persistenceUnitUrls = classLoader.getResources("META-INF/persistence.xml");

                while (persistenceUnitUrls.hasMoreElements()) {

                    URL persistenceUnitUrl = persistenceUnitUrls.nextElement();
                    Document persistenceDom = db.parse(persistenceUnitUrl.openStream());
                    URL rootUrl = getRootJarUrl(persistenceUnitUrl);

                    PersistenceUnitInfo info = PersistenceUnitInfoImpl.getInstance(unitName, persistenceDom, classLoader, rootUrl);
                    if (info != null) {
                        persistenceUnitInfos.put(unitName, info);
                        return info;
                    }

                }

            } catch (IOException ex) {
                throw new Fabric3JpaRuntimeException(ex);
            } catch (ParserConfigurationException ex) {
                throw new Fabric3JpaRuntimeException(ex);
            } catch (SAXException ex) {
                throw new Fabric3JpaRuntimeException(ex);
            }

        }

        throw new Fabric3JpaRuntimeException("Unable to find persistence unit: " + unitName);

    }

    private URL getRootJarUrl(URL persistenceUnitUrl) throws IOException {
        String protocol = persistenceUnitUrl.getProtocol();

        if ("jar".equals(protocol)) {
            JarURLConnection jarURLConnection = (JarURLConnection) persistenceUnitUrl
                    .openConnection();
            return jarURLConnection.getJarFileURL();
        } else if ("file".equals(protocol)) {
            String path = persistenceUnitUrl.getPath();
            return new File(path).getParentFile().getParentFile().toURI().toURL();
        } else if ("zip".equals(protocol)) {
            String path = persistenceUnitUrl.getPath();
            String rootJarUrl = path.substring(0, path.lastIndexOf("META-INF") - 2);
            rootJarUrl = "file:" + rootJarUrl;
            return new URL(rootJarUrl);
        } else {
            throw new Fabric3JpaRuntimeException("Unable to handle protocol: " + protocol);
        }
    }


}
