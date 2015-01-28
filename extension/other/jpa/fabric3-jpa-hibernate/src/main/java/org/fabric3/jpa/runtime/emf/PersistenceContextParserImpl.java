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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.fabric3.datasource.spi.DataSourceRegistry;
import org.oasisopen.sca.annotation.Reference;
import static javax.persistence.spi.PersistenceUnitTransactionType.JTA;
import static javax.persistence.spi.PersistenceUnitTransactionType.RESOURCE_LOCAL;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 *
 */
public class PersistenceContextParserImpl implements PersistenceContextParser {
    private XMLInputFactory factory;
    private DataSourceRegistry registry;

    public PersistenceContextParserImpl(@Reference DataSourceRegistry registry) {
        this.registry = registry;
        factory = XMLInputFactory.newFactory();
    }

    public List<PersistenceUnitInfo> parse(ClassLoader classLoader) throws PersistenceUnitException {
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources("META-INF/persistence.xml");
        } catch (IOException e) {
            throw new PersistenceUnitException(e);
        }
        if (urls == null) {
            throw new PersistenceUnitException("Persistence context (persistence.xml) not found in /META-INF");
        }
        List<PersistenceUnitInfo> infos = new ArrayList<>();
        while(urls.hasMoreElements()) {
            URL url = urls.nextElement();
            URL rootUrl = getRootJarUrl(url);
            InputStream stream = null;
            XMLStreamReader reader = null;
            try {
                stream = url.openStream();
                reader = factory.createXMLStreamReader(stream);

                reader.nextTag();
                PersistenceUnitInfo info = null;
                String version = "2.0";
                boolean parse = true;
                while (parse) {
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
                                    throw new PersistenceUnitException("Invalid persistence.xml found in :"+rootUrl);
                                }
                            }
                            break;
                        case XMLStreamConstants.END_DOCUMENT:
                            parse=false;
                            break;
                    }
                }

            } catch (IOException | XMLStreamException e) {
                throw new PersistenceUnitException(e);
            } finally {
                close(stream, reader);
            }
        }
        return infos;
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
            } catch (IOException | XMLStreamException e) {
                // ignore
                e.printStackTrace();
            }
        }
    }

}
