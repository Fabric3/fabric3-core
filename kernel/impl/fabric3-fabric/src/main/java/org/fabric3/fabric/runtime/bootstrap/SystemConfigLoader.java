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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.runtime.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.fabric3.api.Role;
import org.fabric3.fabric.xml.DocumentLoader;
import org.fabric3.fabric.xml.DocumentLoaderImpl;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.monitor.MonitorConfigurationException;
import org.fabric3.host.runtime.JmxConfiguration;
import org.fabric3.host.runtime.ParseException;
import org.fabric3.host.security.JmxSecurity;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;

import static org.fabric3.host.runtime.BootConstants.RUNTIME_ALIAS;
import static org.fabric3.host.runtime.BootConstants.RUNTIME_MONITOR;

/**
 * Loads the system configuration property for a runtime domain.
 *
 * @version $Revision$ $Date$
 */
public class SystemConfigLoader {
    private static final URI DEFAULT_DOMAIN = URI.create("fabric3://domain");
    private static final String DEFAULT_ZONE = "default.zone";
    private static final int DEFAULT_JMX_PORT = 1199;
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
            Element newRoot = document.createElement("values");
            document.removeChild(oldRoot);
            document.appendChild(newRoot);
            newRoot.appendChild(oldRoot);
            return document;
        } catch (IOException e) {
            throw new ParseException(e);
        } catch (SAXException e) {
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

    public String parseZoneName(Document systemConfig) throws ParseException {
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
            return DEFAULT_ZONE;
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
            } else if ("participant".equalsIgnoreCase(name)) {
                return RuntimeMode.PARTICIPANT;
            }
        }
        return RuntimeMode.VM;
    }

    /**
     * Parses the JMX configuration. If not explicitly configured, security will be disabled and the default range (1199) will be returned.
     *
     * @param systemConfig the system configuration
     * @return the JMX configuration
     * @throws ParseException if there is an error parsing the JMX port range
     */
    public JmxConfiguration parseJmxConfiguration(Document systemConfig) throws ParseException {
        Element root = systemConfig.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("runtime");
        if (nodes.getLength() == 1) {
            Element node = (Element) nodes.item(0);
            JmxSecurity security = parseSecurity(node);


            Set<Role> roles = parseRoles(node);

            String ports = node.getAttribute("jmxPort");
            if (ports.length() == 0) {
                // support alternate syntax
                ports = node.getAttribute("jmx.port");
            }
            if (ports.length() > 0) {
                String[] tokens = ports.split("-");
                int minPort;
                int maxPort;
                if (tokens.length == 1) {
                    // port specified
                    minPort = parsePortNumber(ports);
                    maxPort = minPort;

                } else if (tokens.length == 2) {
                    // port range specified
                    minPort = parsePortNumber(tokens[0]);
                    maxPort = parsePortNumber(tokens[1]);
                } else {
                    throw new ParseException("Invalid JMX port specified in system configuration: " + ports);
                }
                return new JmxConfiguration(security, roles, minPort, maxPort);
            } else {
                return new JmxConfiguration(security, roles, DEFAULT_JMX_PORT, DEFAULT_JMX_PORT);
            }
        } else if (nodes.getLength() == 0) {
            return new JmxConfiguration(JmxSecurity.DISABLED, Collections.<Role>emptySet(), DEFAULT_JMX_PORT, DEFAULT_JMX_PORT);
        }
        throw new ParseException("Invalid system configuration: more than one <runtime> element specified");
    }

    /**
     * Returns the monitor configuration. If not set, null will be returned.
     *
     * @param elementName  the element name of the monitor configuration
     * @param systemConfig the system configuration
     * @return the monitor configuration
     * @throws MonitorConfigurationException if there is an error parsing the monitor configuration
     */
    public Element getMonitorConfiguration(String elementName, Document systemConfig) throws MonitorConfigurationException {
        Element root = systemConfig.getDocumentElement();
        NodeList nodes;
        if (RUNTIME_MONITOR.equals(elementName)) {
            // the configuration element <runtime.monitor> is used as an alias for the runtime monitor name, which is ROOT 
            nodes = root.getElementsByTagName(RUNTIME_ALIAS);
        } else {
            nodes = root.getElementsByTagName(elementName);
        }
        if (nodes.getLength() == 1) {
            Element monitorElement = (Element) nodes.item(0);
            NodeList configurationElements = monitorElement.getElementsByTagName("configuration");
            if (configurationElements.getLength() != 1) {
                throw new MonitorConfigurationException("Invalid system configuration: Only one monitor <configuration> element must be specified");
            } else {
                Element element = (Element) configurationElements.item(0);
                addAppenderReferences(systemConfig, elementName, element);
                return element;
            }
        } else if (nodes.getLength() == 0) {
            return null;
        }
        throw new MonitorConfigurationException("Invalid system configuration: more than one <monitor> element specified");
    }

    /**
     * Returns configured deployment directories or an empty collection.
     *
     * @param systemConfig the system configuration
     * @return the deployment directories
     */
    public List<File> parseDeployDirectories(Document systemConfig) {
        List<File> files = new ArrayList<File>();
        Element root = systemConfig.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("deploy.directory");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            File file = new File(element.getTextContent());
            files.add(file);
        }
        return files;
    }

    /**
     * Adds appender references to the monitor configuration
     *
     * @param systemConfig the system configuration
     * @param loggerName   the logger name
     * @param element      the configuration element
     */
    private void addAppenderReferences(Document systemConfig, String loggerName, Element element) {
        NodeList elements = element.getElementsByTagName("appender");
        List<Element> added = new ArrayList<Element>();
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            Node nameAttribute = node.getAttributes().getNamedItem("name");
            if (nameAttribute != null) {
                String name = nameAttribute.getNodeValue();
                Element reference = systemConfig.createElement("appender-ref");
                reference.setAttribute("ref", name);
                added.add(reference);
            }
        }
        if (!added.isEmpty()) {
            Element root = systemConfig.createElement("logger");
            root.setAttribute("name", loggerName);
            element.appendChild(root);
            for (Element reference : added) {
                root.appendChild(reference);
            }
        }
    }

    private int parsePortNumber(String portVal) {
        int port;
        try {
            port = Integer.parseInt(portVal);
            if (port < 0) {
                throw new IllegalArgumentException("Invalid JMX port number specified in runtime.properties:" + port);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid JMX port", e);
        }
        return port;
    }

    private Set<Role> parseRoles(Element node) {
        String[] rolesString = node.getAttribute("jmx.access.roles").split(",");
        Set<Role> roles = new HashSet<Role>();
        for (String s : rolesString) {
            roles.add(new Role(s.trim()));
        }
        return roles;
    }

    private JmxSecurity parseSecurity(Element node) throws ParseException {
        String securityString = node.getAttribute("jmx.security");
        if (securityString.length() > 0) {
            try {
                return JmxSecurity.valueOf(securityString.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ParseException("Invalid JMX security setting:" + securityString);
            }
        }
        return JmxSecurity.DISABLED;
    }

}