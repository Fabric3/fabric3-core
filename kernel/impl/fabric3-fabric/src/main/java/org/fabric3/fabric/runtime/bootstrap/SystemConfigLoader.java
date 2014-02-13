/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.runtime.bootstrap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.fabric.xml.DocumentLoader;
import org.fabric3.fabric.xml.DocumentLoaderImpl;
import org.fabric3.api.host.Environment;
import org.fabric3.api.host.Names;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.ParseException;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.oasisopen.sca.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Loads the system configuration property for a runtime domain.
 */
public class SystemConfigLoader {
    private static final URI DEFAULT_DOMAIN = URI.create("fabric3://domain");
    private DocumentLoader loader;

    public SystemConfigLoader() {
        loader = new DocumentLoaderImpl();
    }

    /**
     * Loads the system configuration value from a systemConfig.xml file or creates a default value if the file does not exist.
     *
     * @param configDirectory the directory where the file is located
     * @return the loaded value
     * @throws ParseException if an error parsing the file contents is encountered
     */
    public Document loadSystemConfig(File configDirectory) throws ParseException {
        File systemConfig = new File(configDirectory, "systemConfig.xml");
        if (systemConfig.exists()) {
            try {
                URL url = systemConfig.toURI().toURL();
                Source source = new UrlSource(url);
                return loadSystemConfig(source);
            } catch (MalformedURLException e) {
                throw new ParseException(e);
            }
        }
        return createDefaultSystemConfig();
    }

    /**
     * Returns a configuration property value for the runtime domain from the given source.
     *
     * @param source the source to read
     * @return the domain configuration property
     * @throws ParseException if an error reading the source is encountered
     */
    public Document loadSystemConfig(Source source) throws ParseException {
        try {
            InputSource inputSource = new InputSource(source.openStream());
            Document document = loader.load(inputSource, true);
            // all properties have a root <values> element, append the existing root to it. The existing root will be taken as a property <value>.
            Element oldRoot = document.getDocumentElement();
            boolean hasNamespaces = oldRoot.getNamespaceURI() != null && !"".equals(oldRoot.getNamespaceURI());
            Element newRoot = document.createElementNS(org.fabric3.api.Namespaces.F3, "values");
            newRoot.setAttribute("xmlns:sca", Constants.SCA_NS);
            document.removeChild(oldRoot);
            document.appendChild(newRoot);
            newRoot.appendChild(oldRoot);
            if (!hasNamespaces) {
                // System config did not specify namespaces, add them
                // Note that namespaces are not added to attributes since the latter do not inherit the default namespace:
                // http://www.w3.org/TR/REC-xml-names/#defaulting (the default namespace does not apply to attribute names)
                // If namespaces were added to attributes, consistency would require users to manually add them to all attributes in a systemConfig
                // that is namespace-aware and hence is not updated using DocumentLoader.addNamespace().
                loader.addNamespace(document, oldRoot, org.fabric3.api.Namespaces.F3);
            }
            return document;
        } catch (IOException | SAXException e) {
            throw new ParseException(e);
        }
    }

    /**
     * Creates a default configuration property value for the runtime domain.
     *
     * @return a document representing the configuration property
     */
    public Document createDefaultSystemConfig() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().newDocument();
            Element root = document.createElement("values");
            document.appendChild(root);
            Element config = document.createElement("config");
            root.appendChild(config);
            return document;
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the configured domain name the runtime should join. If not configured, the default domain name will be returned.
     *
     * @param systemConfig the system configuration
     * @return the domain name
     * @throws ParseException if there is an error parsing the domain name
     */
    public URI parseDomainName(Document systemConfig) throws ParseException {
        Element root = systemConfig.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("runtime");
        if (nodes.getLength() == 1) {
            Element node = (Element) nodes.item(0);
            String name = node.getAttribute("domain");
            if (name.length() > 0) {
                try {
                    URI uri = new URI("fabric3://" + name);
                    if (uri.getHost() == null) {
                        throw new ParseException("Invalid domain name specified in system configuration. Domain names must be a valid URI host.");
                    }
                    return uri;
                } catch (URISyntaxException e) {
                    throw new ParseException("Invalid domain name specified in system configuration", e);
                }
            } else {
                return DEFAULT_DOMAIN;
            }
        } else if (nodes.getLength() == 0) {
            return DEFAULT_DOMAIN;
        }
        throw new ParseException("Invalid system configuration: more than one <runtime> element specified");
    }

    /**
     * Returns the product name. If not configured, "Fabric3" will be returned.
     *
     * @param systemConfig the system configuration
     * @return the product name
     * @throws ParseException if there is an error parsing the domain name
     */
    public String parseProductName(Document systemConfig) throws ParseException {
        Element root = systemConfig.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("runtime");
        if (nodes.getLength() == 1) {
            Element node = (Element) nodes.item(0);
            String name = node.getAttribute("product");
            if (name.length() > 0) {
                return name;
            } else {
                return "Fabric3";
            }
        } else if (nodes.getLength() == 0) {
            return "Fabric3";
        }
        throw new ParseException("Invalid system configuration: more than one <runtime> element specified");
    }

    public String parseZoneName(Document systemConfig, RuntimeMode mode) throws ParseException {
        if (RuntimeMode.VM == mode) {
            return Names.LOCAL_ZONE;
        }
        Element root = systemConfig.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("zoneName");
        if (nodes.getLength() == 1) {
            Element node = (Element) nodes.item(0);
            String name = node.getTextContent();
            if (name != null) {
                name = name.trim();
            }
            return name;
        } else if (nodes.getLength() == 0) {
            return Names.DEFAULT_ZONE;
        }
        throw new ParseException("Invalid system configuration: more than one <runtime> element specified");
    }

    /**
     * Returns the configured runtime mode. If not configured, {@link RuntimeMode#VM} will be returned.
     *
     * @param systemConfig the system configuration
     * @return the domain name
     * @throws ParseException if there is an error parsing the domain name
     */
    public RuntimeMode parseRuntimeMode(Document systemConfig) throws ParseException {
        Element root = systemConfig.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("runtime");
        if (nodes.getLength() == 1) {
            Element node = (Element) nodes.item(0);
            String name = node.getAttribute("mode");
            if ("controller".equalsIgnoreCase(name)) {
                return RuntimeMode.CONTROLLER;
            } else if ("node".equalsIgnoreCase(name)) {
                return RuntimeMode.NODE;
            } else if ("participant".equalsIgnoreCase(name)) {
                return RuntimeMode.PARTICIPANT;
            }
        }
        return RuntimeMode.VM;
    }

    /**
     * Returns the runtime environment. If one is not explicitly configured, the default {@link Environment#PRODUCTION} will be returned.
     *
     * @param systemConfig the system configuration
     * @return the parsed runtime environment
     */
    public String parseEnvironment(Document systemConfig) {
        Element root = systemConfig.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("runtime");
        if (nodes.getLength() == 1) {
            Element node = (Element) nodes.item(0);
            String environment = node.getAttribute("environment");
            if (environment.length() > 0) {
                return environment;
            }
        }
        return Environment.PRODUCTION;
    }

    /**
     * Returns configured deployment directories or an empty collection.
     *
     * @param systemConfig the system configuration
     * @return the deployment directories
     */
    public List<File> parseDeployDirectories(Document systemConfig) {
        List<File> files = new ArrayList<>();
        Element root = systemConfig.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("deploy.directory");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            File file = new File(element.getTextContent());
            files.add(file);
        }
        return files;
    }

}