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
package org.fabric3.binding.ws.metro.generator.resolver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.fabric3.binding.ws.metro.generator.WsdlElement;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;

/**
 * Default EndpointResolver implementation.
 *
 * @version $Rev$ $Date$
 */
public class EndpointResolverImpl implements EndpointResolver {
    private static final QName SOAP11_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address");
    private static final QName SOAP12_ADDRESS = new QName("http://www.w3.org/2003/05/soap/bindings/HTTP/", "address");

    public ServiceEndpointDefinition resolveServiceEndpoint(WsdlElement wsdlElement, Definition definition) throws EndpointResolutionException {
        return resolveServiceEndpoint(wsdlElement, definition, null);
    }

    public ServiceEndpointDefinition resolveServiceEndpoint(WsdlElement wsdlElement, Definition wsdl, URI uri) throws EndpointResolutionException {
        QName serviceName = wsdlElement.getServiceName();
        QName portName = wsdlElement.getPortName();
        Port port = resolvePort(serviceName, portName, wsdl);
        URI servicePath;
        if (uri == null) {
            URL url = getAddress(port);
            servicePath = URI.create(url.getPath());
        } else {
            servicePath = uri;
        }
        return new ServiceEndpointDefinition(serviceName, portName, servicePath);
    }

    public ReferenceEndpointDefinition resolveReferenceEndpoint(WsdlElement wsdlElement, Definition wsdl) throws EndpointResolutionException {
        QName serviceName = wsdlElement.getServiceName();
        QName portName = wsdlElement.getPortName();
        Port port = resolvePort(serviceName, portName, wsdl);
        URL url = getAddress(port);
        QName portTypeName = port.getBinding().getPortType().getQName();
        return new ReferenceEndpointDefinition(serviceName, false, portName, portTypeName, url);
    }

    private Port resolvePort(QName serviceName, QName portName, Definition wsdl) throws EndpointResolutionException {
        Service service = wsdl.getService(serviceName);
        if (service == null) {
            throw new EndpointResolutionException("WSDL service not found: " + serviceName);
        }
        Port port = service.getPort(portName.getLocalPart());
        if (port == null) {
            throw new EndpointResolutionException("WSDL port not found: " + portName);
        }
        return port;
    }

    private URL getAddress(Port port) throws EndpointResolutionException {
        for (Object o : port.getExtensibilityElements()) {
            ExtensibilityElement element = (ExtensibilityElement) o;
            QName elementType = element.getElementType();
            if (SOAP11_ADDRESS.equals(elementType) || SOAP12_ADDRESS.equals(elementType)) {
                try {
                    return new URL(((SOAPAddress) element).getLocationURI());
                } catch (MalformedURLException e) {
                    throw new EndpointResolutionException("Invalid URL specified for port " + port.getName(), e);
                }
            }
        }
        throw new EndpointResolutionException("SOAP address not found on port " + port.getName());
    }

}
