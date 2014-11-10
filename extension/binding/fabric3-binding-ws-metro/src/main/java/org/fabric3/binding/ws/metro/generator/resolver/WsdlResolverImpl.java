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
 */
package org.fabric3.binding.ws.metro.generator.resolver;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.wsdl.contribution.BindingSymbol;
import org.fabric3.wsdl.contribution.PortSymbol;
import org.fabric3.wsdl.contribution.ServiceSymbol;
import org.fabric3.wsdl.contribution.WsdlSymbol;
import org.fabric3.wsdl.contribution.Wsdl4JFactory;
import org.oasisopen.sca.annotation.Reference;

/**
 * Resolves parsed WSDLs against an external location or those visible to the current contribution installed in the domain.
 */
public class WsdlResolverImpl implements WsdlResolver {
    private MetaDataStore store;
    private Wsdl4JFactory wsdlFactory;

    public WsdlResolverImpl(@Reference MetaDataStore store, @Reference Wsdl4JFactory wsdlFactory) throws WSDLException {
        this.store = store;
        this.wsdlFactory = wsdlFactory;
    }

    public Definition parseWsdl(URL wsdlLocation) throws WsdlResolutionException {
        try {
            WSDLReader reader = wsdlFactory.newReader();
            return reader.readWSDL(wsdlLocation.toURI().toString());
        } catch (WSDLException | URISyntaxException e) {
            throw new WsdlResolutionException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public Definition resolveWsdl(URI contributionUri, QName wsdlName) throws WsdlResolutionException {
        WsdlSymbol symbol = new WsdlSymbol(wsdlName);
        try {
            ResourceElement<WsdlSymbol, Definition> element = store.find(contributionUri, Definition.class, symbol);
            if (element == null) {
                throw new WsdlResolutionException("WSDL not found: " + wsdlName);
            }
            return element.getValue();
        } catch (StoreException e) {
            throw new WsdlResolutionException(e);
        }
    }

    public Definition resolveWsdlByPortName(URI contributionUri, QName portName) throws WsdlResolutionException {
        PortSymbol symbol = new PortSymbol(portName);
        ResourceElement<PortSymbol, Port> resourceElement;
        try {
            resourceElement = store.find(contributionUri, Port.class, symbol);
        } catch (StoreException e) {
            throw new WsdlResolutionException("Error resolving port: " + portName, e);
        }
        if (resourceElement == null) {
            throw new WsdlResolutionException("WSDL port not found: " + portName);
        }
        Resource resource = resourceElement.getResource();
        for (ResourceElement<?, ?> element : resource.getResourceElements()) {
            if (element.getSymbol() instanceof WsdlSymbol) {
                return (Definition) element.getValue();
            }
        }
        throw new WsdlResolutionException("WSDL for port not found: " + portName);
    }

    public Definition resolveWsdlByServiceName(URI contributionUri, QName serviceName) throws WsdlResolutionException {
        ServiceSymbol symbol = new ServiceSymbol(serviceName);
        ResourceElement<ServiceSymbol, Service> resourceElement;
        try {
            resourceElement = store.find(contributionUri, Service.class, symbol);
        } catch (StoreException e) {
            throw new WsdlResolutionException("Error resolving service: " + serviceName, e);
        }
        if (resourceElement == null) {
            throw new WsdlResolutionException("WSDL service not found: " + serviceName);
        }
        Resource resource = resourceElement.getResource();
        for (ResourceElement<?, ?> element : resource.getResourceElements()) {
            if (element.getSymbol() instanceof WsdlSymbol) {
                return (Definition) element.getValue();
            }
        }
        throw new WsdlResolutionException("WSDL for service not found: " + serviceName);
    }

    public Definition resolveWsdlByBindingName(URI contributionUri, QName bindingName) throws WsdlResolutionException {
        BindingSymbol symbol = new BindingSymbol(bindingName);
        ResourceElement<BindingSymbol, Binding> resourceElement;
        try {
            resourceElement = store.find(contributionUri, Binding.class, symbol);
        } catch (StoreException e) {
            throw new WsdlResolutionException("Error resolving binding: " + bindingName, e);
        }
        if (resourceElement == null) {
            throw new WsdlResolutionException("WSDL binding not found: " + bindingName);
        }
        Resource resource = resourceElement.getResource();
        for (ResourceElement<?, ?> element : resource.getResourceElements()) {
            if (element.getSymbol() instanceof WsdlSymbol) {
                return (Definition) element.getValue();
            }
        }
        throw new WsdlResolutionException("WSDL for binding not found: " + bindingName);
    }
}