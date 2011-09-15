/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.spi.xml.XMLFactory;

import static javax.persistence.spi.PersistenceUnitTransactionType.JTA;
import static javax.persistence.spi.PersistenceUnitTransactionType.RESOURCE_LOCAL;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceContextParserImpl implements PersistenceContextParser {
    private XMLInputFactory factory;
    private DataSourceRegistry registry;

    public PersistenceContextParserImpl(@Reference DataSourceRegistry registry, @Reference XMLFactory xmlFactory) {
        this.registry = registry;
        factory = xmlFactory.newInputFactoryInstance();
    }

    public List<PersistenceUnitInfo> parse(ClassLoader classLoader) throws PersistenceUnitException {
        URL url = classLoader.getResource("META-INF/persistence.xml");
        if (url == null) {
            throw new PersistenceUnitException("Persistence context (persistence.xml) not found in /META-INF");
        }
        URL rootUrl = getRootJarUrl(url);
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = url.openStream();
            reader = factory.createXMLStreamReader(stream);
            List<PersistenceUnitInfo> infos = new ArrayList<PersistenceUnitInfo>();
            reader.nextTag();
            PersistenceUnitInfo info = null;
            String version = "2.0";
            while (true) {
                int event = reader.next();
                switch (event) {
                case START_ELEMENT:
                    if ("persistence".equals(reader.getName().getLocalPart())) {
                        String versionAttr = reader.getAttributeValue(null, "version");
                        if (versionAttr != null) {
                            version = versionAttr;
                        }
                    } else if ("persistence-unit".equals(reader.getName().getLocalPart())) {
                        info = parsePersistenceUnit(reader, classLoader, rootUrl, version);
                        infos.add(info);
                    }
                    break;
                case END_ELEMENT:
                    if ("persistence-unit".equals(reader.getName().getLocalPart())) {
                        if (info == null) {
                            throw new PersistenceUnitException("Invalid persistence.xml");
                        }
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return infos;
                }
            }
        } catch (IOException e) {
            throw new PersistenceUnitException(e);
        } catch (XMLStreamException e) {
            throw new PersistenceUnitException(e);
        } finally {
            close(stream, reader);
        }
    }

    private URL getRootJarUrl(URL url) throws PersistenceUnitException {
        try {
            String protocol = url.getProtocol();
            if ("jar".equals(protocol)) {
                JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                return jarURLConnection.getJarFileURL();
            } else if ("file".equals(protocol)) {
                String path = url.getPath();
                return new File(path).getParentFile().getParentFile().toURI().toURL();
            } else if ("zip".equals(protocol)) {
                String path = url.getPath();
                String rootJarUrl = path.substring(0, path.lastIndexOf("META-INF") - 2);
                rootJarUrl = "file:" + rootJarUrl;
                return new URL(rootJarUrl);
            } else {
                throw new PersistenceUnitException("Unknown protocol: " + protocol);
            }
        } catch (IOException e) {
            throw new PersistenceUnitException(e);
        }
    }

    private PersistenceUnitInfo parsePersistenceUnit(XMLStreamReader reader, ClassLoader classLoader, URL rootUrl, String version)
            throws XMLStreamException, PersistenceUnitException, MalformedURLException {
        String name = reader.getAttributeValue(null, "name");
        String trxAttr = reader.getAttributeValue(null, "transaction-type");
        PersistenceUnitTransactionType trxType = "JTA".equals(trxAttr) ? JTA : RESOURCE_LOCAL;
        Fabric3PersistenceUnitInfo info = new Fabric3PersistenceUnitInfo(name);
        info.setPersistenceXMLSchemaVersion(version);
        info.setTrxType(trxType);
        info.setClassLoader(classLoader);
        info.setRootUrl(rootUrl);
        while (true) {
            int event = reader.nextTag();
            switch (event) {
            case START_ELEMENT:
                if ("jta-data-source".equals(reader.getName().getLocalPart())) {
                    DataSource dataSource = getDataSource(reader);
                    info.setJtaDataSource(dataSource);
                } else if ("non-jta-data-source".equals(reader.getName().getLocalPart())) {
                    DataSource dataSource = getDataSource(reader);
                    info.setNonJtaDataSource(dataSource);
                } else if ("provider".equals(reader.getName().getLocalPart())) {
                    String className = reader.getElementText();
                    info.setPersistenceProviderClassName(className);
                } else if ("class".equals(reader.getName().getLocalPart())) {
                    String className = reader.getElementText();
                    info.addManagedClass(className);
                } else if ("properties".equals(reader.getName().getLocalPart())) {
                    parseProperties(info, reader);
                } else if ("mapping-file".equals(reader.getName().getLocalPart())) {
                    String file = reader.getElementText();
                    info.addMappingFile(file);
                } else if ("jar-file".equals(reader.getName().getLocalPart())) {
                    URL file = new File(reader.getElementText()).toURI().toURL();
                    info.addJarFileUrl(file);
                } else if ("exclude-unlisted-classes".equals(reader.getName().getLocalPart())) {
                    boolean exclude = Boolean.parseBoolean(reader.getElementText());
                    info.setExcludeUnlistedClasses(exclude);
                } else if ("shared-cache-mode".equals(reader.getName().getLocalPart())) {
                    String value = reader.getElementText();
                    try {
                        SharedCacheMode mode = SharedCacheMode.valueOf(value);
                        info.setSharedCacheMode(mode);
                    } catch (IllegalArgumentException e) {
                        throw new PersistenceUnitException("Illegal shared cache mode: " + value);
                    }
                } else if ("validation-mode".equals(reader.getName().getLocalPart())) {
                    String value = reader.getElementText();
                    try {
                        ValidationMode mode = ValidationMode.valueOf(value);
                        info.setValidationMode(mode);
                    } catch (IllegalArgumentException e) {
                        throw new PersistenceUnitException("Illegal validation mode: " + value);
                    }
                }
                break;
            case END_ELEMENT:
                if ("persistence-unit".equals(reader.getName().getLocalPart())) {
                    return info;
                }
                break;
            }
        }
    }

    private DataSource getDataSource(XMLStreamReader reader) throws PersistenceUnitException {
        try {
            String dataSourceName = reader.getElementText();
            DataSource dataSource = registry.getDataSource(dataSourceName);
            if (dataSource == null) {
                throw new PersistenceUnitException("DataSource not found: " + dataSourceName);
            }
            return dataSource;
        } catch (XMLStreamException e) {
            throw new PersistenceUnitException(e);
        }
    }

    private void parseProperties(Fabric3PersistenceUnitInfo info, XMLStreamReader reader) throws XMLStreamException {
        while (true) {
            int event = reader.next();
            switch (event) {
            case START_ELEMENT:
                if ("property".equals(reader.getName().getLocalPart())) {
                    String name = reader.getAttributeValue(null, "name");
                    String value = reader.getAttributeValue(null, "value");
                    info.addProperty(name, value);
                }
                break;
            case END_ELEMENT:
                if ("properties".equals(reader.getName().getLocalPart())) {
                    return;
                }
            }
        }
    }

    private void close(InputStream stream, XMLStreamReader reader) {
        if (stream != null) {
            try {
                stream.close();
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // ignore
                e.printStackTrace();
            } catch (XMLStreamException e) {
                // ignore
                e.printStackTrace();
            }
        }
    }

}
