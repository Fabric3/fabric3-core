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
package org.fabric3.binding.ws.metro.runtime.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;

import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import com.sun.xml.wss.SecurityEnvironment;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Creates a service proxy that can be shared among invocation chains of a wire. The proxy must be lazily created as opposed to during wire attachment
 * as as the service WSDL is accessed from the endpoint address, which may not be provisioned at that time.
 *
 * @version $Rev$ $Date$
 */
public class MetroProxyObjectFactory implements ObjectFactory<Object> {
    private static final QName DEFINITIONS = new QName("http://schemas.xmlsoap.org/wsdl/", "definitions");
    private static final EntityResolver RESOLVER = new NullResolver();

    private URL wsdlLocation;
    private QName serviceName;
    private boolean serviceNameDefault;
    private QName portName;
    private QName portTypeName;
    private Class<?> seiClass;
    private WebServiceFeature[] features;
    private URL wsitConfiguration;
    private ExecutorService executorService;
    private SecurityEnvironment securityEnvironment;
    private XMLInputFactory xmlInputFactory;
    private Object proxy;
    private URL endpointUrl;

    /**
     * Constructor.
     *
     * @param endpointDefinition  the target endpoint definition
     * @param wsdlLocation        the location of the target service WSDL
     * @param wsitConfiguration   WSIT policy configuration for the proxy, or null if policy is not configured
     * @param seiClass            the target SEI
     * @param features            web services features to enable on the generated proxy
     * @param handlers            the handlers or null
     * @param executorService     the executor service used for dispatching invocations
     * @param securityEnvironment the Metro host runtime security SPI implementation
     * @param xmlInputFactory     the StAX XML factory to use for WSDL parsing
     */
    public MetroProxyObjectFactory(ReferenceEndpointDefinition endpointDefinition,
                                   URL wsdlLocation,
                                   URL wsitConfiguration,
                                   Class<?> seiClass,
                                   WebServiceFeature[] features,
                                   List<Handler> handlers, ExecutorService executorService,
                                   SecurityEnvironment securityEnvironment,
                                   XMLInputFactory xmlInputFactory) {
        this.serviceName = endpointDefinition.getServiceName();
        this.serviceNameDefault = endpointDefinition.isDefaultServiceName();
        this.portTypeName = endpointDefinition.getPortTypeName();
        this.portName = endpointDefinition.getPortName();
        this.endpointUrl = endpointDefinition.getUrl();
        this.wsdlLocation = wsdlLocation;
        this.seiClass = seiClass;
        this.features = features;
        this.wsitConfiguration = wsitConfiguration;
        this.executorService = executorService;
        this.securityEnvironment = securityEnvironment;
        this.xmlInputFactory = xmlInputFactory;
    }

    public Object getInstance() throws ObjectCreationException {
        if (proxy == null) {
            // there is a possibility more than one proxy will be created but since this does not have side-effects, avoid synchronization
            proxy = createProxy();
        }
        return proxy;
    }

    /**
     * Lazily creates the service proxy. Proxy creation is done during the first invocation as the target service may not be available when the client
     * that the proxy is to be injected into is instantiated. The proxy is later cached for subsequent invocations.
     *
     * @return the web service proxy
     * @throws ObjectCreationException if there was an error creating the proxy
     */
    private Object createProxy() throws ObjectCreationException {
        if (wsdlLocation == null) {
            wsdlLocation = calculateDefaultWsdlLocation();
        }

        // Metro requires library classes to be visible to the application classloader. If executing in an environment that supports classloader
        // isolation, dynamically update the application classloader by setting a parent to the Metro classloader.
        ClassLoader seiClassLoader = seiClass.getClassLoader();
        if (seiClassLoader instanceof MultiParentClassLoader) {
            MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) seiClassLoader;
            ClassLoader extensionCl = getClass().getClassLoader();
            if (!multiParentClassLoader.getParents().contains(extensionCl)) {
                multiParentClassLoader.addParent(extensionCl);
            }
        }

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(seiClassLoader);
            Service service;
            WSService.InitParams params = new WSService.InitParams();
            WsitClientConfigurationContainer container;
            if (wsitConfiguration != null) {
                // Policy configured
                container = new WsitClientConfigurationContainer(wsitConfiguration, securityEnvironment);
            } else {
                // No policy
                container = new WsitClientConfigurationContainer(securityEnvironment);
            }
            params.setContainer(container);
            try {
                service = WSService.create(wsdlLocation, serviceName, params);
            } catch (WebServiceException e) {
                service = getWsdlServiceName(e, params);
            }
            // use the kernel scheduler for dispatching
            service.setExecutor(executorService);
            service.getPorts();
            return service.getPort(portName, seiClass, features);
        } catch (InaccessibleWSDLException e) {
            throw new ObjectCreationException(e);
        } catch (MalformedURLException e) {
            throw new ObjectCreationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * This is a a bit of a hack. JAX-WS and WCF (.NET) have different default service naming conventions when mapping from implementation artifacts
     * (Java and C#/VB, etc. classes). In SCA, a reference configured with the web services binding often only specifies the target portType name as
     * only the port interface is provided (i.e. the Java interface for the reference). When creating a proxy, Metro requires the service name for the
     * portType to be specified. Since WCF uses different defaulting rules, it is not possible to calculate the service name according to JAX-WS
     * rules. When the JAX-WS API is used directly, this is not a problem as the service proxy is created by using a generated service client class
     * marked with the <code>WebServiceClient</code> annotation which explicitly declares the service name. However, in SCA, the portType interface is
     * provided, not the generated service client class.
     * <p/>
     * Rather than requiring users to explicitly declare the service in this case, the target WSDL is introspected for a service name. This will only
     * be done if: the original service name is not valid (i.e. the web service exception triggering this procedure resulted from an invalid service
     * name during proxy generation); and if the original provided service name is only a default and may be overriden. Also note this procedure will
     * only return a service name if the target WSDL contains only one service which uses the portType (otherwise it would be impossible to select the
     * correct service). Barring this, a user would need to explicitly declare the service name via a JAX-WS annotation or the wsdlElement attribute
     * on binding.ws.
     *
     * @param e      the WebServiceException  triggering this operation
     * @param params any initialization parameters to use when attempting to create a proxy using an introspected service name
     * @return the Service instance for creating a proxy
     * @throws ObjectCreationException if a service name could not be introspected
     * @throws WebServiceException     if the original WebServiceException  was not the result of an invalid operation or if there was an error
     *                                 creating the Service instance
     */
    private Service getWsdlServiceName(WebServiceException e, WSService.InitParams params) throws ObjectCreationException, WebServiceException {
        Service service;
        // Only calculate a default name if the original can be overriden and the WebServiceException results from an invalid service name; otherwise
        // re-throw the exception
        if (!this.serviceNameDefault || !isInvalidServiceName(e)) {
            throw e;
        }
        InputStream stream = null;
        try {
            // Locate the service name by looking up the port type.
            // Use a Set to filter duplicate service names in the case that multiple ports from the same service use the same portType.
            // In that case, the service can be computed
            Set<QName> found = new HashSet<QName>();
            stream = wsdlLocation.openStream();
            StreamSource source = new StreamSource(stream);
            WSDLModel model = RuntimeWSDLParser.parse(wsdlLocation, source, RESOLVER, false, null);
            for (WSDLService wsdlService : model.getServices().values()) {
                for (WSDLPort wsdlPort : wsdlService.getPorts()) {
                    if (wsdlPort.getBinding().getPortType().getName().equals(portTypeName)) {
                        found.add(wsdlService.getName());
                    }
                }
            }
            if (found.size() > 1) {
                throw new ObjectCreationException("Cannot determine the default service name as multiple ports using portType " + portTypeName +
                                                          " were found in the WSDL document: " + wsdlLocation);
            } else if (found.isEmpty()) {
                throw new ObjectCreationException("No default service for portType" + portTypeName + " found in WSDL: " + wsdlLocation);
            } else {
                // retry creating the service
                QName defaultServiceName = found.iterator().next();
                service = WSService.create(wsdlLocation, defaultServiceName, params);
            }
        } catch (IOException e1) {
            throw new ObjectCreationException(e1);
        } catch (SAXException e1) {
            throw new ObjectCreationException(e1);
        } catch (XMLStreamException e1) {
            throw new ObjectCreationException(e1);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e3) {
                // ignore
            }
        }
        return service;
    }

    /**
     * Determines the default WSDL location if one is not provided.
     *
     * @return the WSDL URL
     * @throws ObjectCreationException if there is an error determining the WSDL location
     */
    private URL calculateDefaultWsdlLocation() throws ObjectCreationException {
        try {
            // default to the target URL with ?wsdl appended since most WS stacks support this
            URL wsdlUrl = new URL(endpointUrl.toString() + "?wsdl");
            if (isWsdl(wsdlUrl)) {
                return wsdlUrl;
            }
            // Try WCF (.NET) default WSDL URL when service metadata is enabled, which is the base service address + ?wsdl.
            // For example, http://foo.com/service/MyService coverts to http://foo.com/service?wsdl
            String str = endpointUrl.toString();
            int pos = str.lastIndexOf("/");
            if (pos > 0) {
                wsdlUrl = new URL(str.substring(0, pos) + "?wsdl");
                if (isWsdl(wsdlUrl)) {
                    return wsdlUrl;
                }
            }
        } catch (MalformedURLException e) {
            throw new ObjectCreationException(e);
        }
        throw new ObjectCreationException(
                "The web service endpoint " + endpointUrl + " does not expose a valid WSDL at a known metadata location, e.g. <service url>?wsdl. " +
                        "Check to make sure the endpoint address is correct. If it is, please specify a valid location using the @WebService " +
                        "annotation on the reference interface.");
    }

    /**
     * Determines if the content of the specified URL is a WSDL document.
     *
     * @param wsdlUrl the URL
     * @return true if the content is a WSDL document
     */
    private boolean isWsdl(URL wsdlUrl) {
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = wsdlUrl.openStream();
            reader = xmlInputFactory.createXMLStreamReader(stream);
            reader.nextTag();
            if (DEFINITIONS.equals(reader.getName())) {
                return true;
            }
        } catch (NullPointerException e) {
            // ignore thrown by URL.openStream(..)
        } catch (IOException e) {
            // ignore
        } catch (XMLStreamException e) {
            // ignore
        } finally {
            close(stream);
            close(reader);
        }
        return false;
    }

    /**
     * Returns true if the WebServiceException was thrown as a result of an invalid service name.
     *
     * @param e the exception
     * @return true if the WebServiceException was thrown as a result of an invalid service name
     * @throws ObjectCreationException if the exception message cannot be parsed
     */
    private boolean isInvalidServiceName(WebServiceException e) throws ObjectCreationException {
        String message = ClientMessages.INVALID_SERVICE_NAME(serviceName, null);
        int index = message.indexOf("null");
        if (index < 1) {
            throw new ObjectCreationException("Unable to parse error message after proxy creation error was thrown: " + message, e);
        }
        message = message.substring(0, index);
        return e.getMessage().contains(message);
    }

    private void close(XMLStreamReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (XMLStreamException e) {
            // ignore
        }
    }

    private void close(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static class NullResolver implements EntityResolver {

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return null;
        }
    }

}
