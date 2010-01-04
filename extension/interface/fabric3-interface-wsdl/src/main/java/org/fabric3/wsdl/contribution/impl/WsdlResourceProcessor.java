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
package org.fabric3.wsdl.contribution.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.wsdl.contribution.PortSymbol;
import org.fabric3.wsdl.contribution.PortTypeSymbol;
import org.fabric3.wsdl.contribution.WsdlResourceProcessorExtension;
import org.fabric3.wsdl.contribution.WsdlServiceContractSymbol;
import org.fabric3.wsdl.contribution.WsdlSymbol;
import org.fabric3.wsdl.factory.Wsdl4JFactory;
import org.fabric3.wsdl.model.WsdlServiceContract;
import org.fabric3.wsdl.processor.WsdlContractProcessor;

/**
 * Indexes and processes a WSDL document, referenced schemas, and introspected service contracts deriving from port types in a contribution. This
 * implementation uses the WSDL4J to represent the document.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class WsdlResourceProcessor implements ResourceProcessor {
    private static final QName DEFINITIONS = new QName("http://schemas.xmlsoap.org/wsdl", "definitions");
    private static final String MIME_TYPE = "text/wsdl+xml";

    private ProcessorRegistry registry;
    private WsdlContractProcessor processor;
    private Wsdl4JFactory factory;
    private List<WsdlResourceProcessorExtension> extensions = new ArrayList<WsdlResourceProcessorExtension>();

    public WsdlResourceProcessor(@Reference ProcessorRegistry registry,
                                 @Reference WsdlContractProcessor processor,
                                 @Reference Wsdl4JFactory factory) {
        this.registry = registry;
        this.processor = processor;
        this.factory = factory;
    }

    @Reference(required = false)
    public void setExtensions(List<WsdlResourceProcessorExtension> extensions) {
        this.extensions = extensions;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public String getContentType() {
        return MIME_TYPE;
    }

    public QName getType() {
        return DEFINITIONS;
    }

    public void index(Contribution contribution, URL url, IntrospectionContext context) throws InstallException {
        InputStream stream = null;
        try {
            stream = url.openStream();
            // eagerly process the WSDL since port types need to be available during contribution processing.
            Resource resource = new Resource(url, MIME_TYPE);
            parse(resource, context);
            resource.setProcessed(true);
            contribution.addResource(resource);
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void process(Resource resource, IntrospectionContext context) throws InstallException {
        // no-op since the WSDL was eagerly processed.
    }

    @SuppressWarnings({"unchecked"})
    private void parse(Resource resource, IntrospectionContext context) throws InstallException {
        // parse the WSDL
        URL wsdlLocation = resource.getUrl();
        Definition definition = parseWsdl(wsdlLocation);
        QName wsdlQName = definition.getQName();
        WsdlSymbol wsdlSymbol = new WsdlSymbol(wsdlQName);
        ResourceElement<WsdlSymbol, Definition> wsdlElement = new ResourceElement<WsdlSymbol, Definition>(wsdlSymbol, definition);
        resource.addResourceElement(wsdlElement);

        Map<String, Service> services = definition.getServices();
        for (Service service : services.values()) {
            Map<String, Port> ports = service.getPorts();
            for (Port port : ports.values()) {
                QName name = new QName(definition.getTargetNamespace(), port.getName());
                PortSymbol portSymbol = new PortSymbol(name);
                ResourceElement<PortSymbol, Port> portElement = new ResourceElement<PortSymbol, Port>(portSymbol, port);
                resource.addResourceElement(portElement);
            }
        }
        for (Object object : definition.getPortTypes().values()) {
            PortType portType = (PortType) object;
            QName name = portType.getQName();
            PortTypeSymbol symbol = new PortTypeSymbol(name);
            ResourceElement<PortTypeSymbol, PortType> element = new ResourceElement<PortTypeSymbol, PortType>(symbol, portType);
            resource.addResourceElement(element);
        }
        // parse WSDL schemas
        XmlSchemaCollection schemaCollection = parseSchema(definition);
        // TODO index XML schema elements and types?

        // introspect port type service contracts
        for (Object object : definition.getPortTypes().values()) {
            PortType portType = (PortType) object;
            WsdlServiceContract contract = processor.introspect(portType, wsdlQName, schemaCollection, context);
            QName name = portType.getQName();
            WsdlServiceContractSymbol symbol = new WsdlServiceContractSymbol(name);
            ResourceElement<WsdlServiceContractSymbol, WsdlServiceContract> element =
                    new ResourceElement<WsdlServiceContractSymbol, WsdlServiceContract>(symbol, contract);
            resource.addResourceElement(element);
        }

        // callback processor extensions
        for (WsdlResourceProcessorExtension extension : extensions) {
            extension.process(resource, definition);
        }
    }

    private Definition parseWsdl(URL wsdlLocation) throws InstallException {
        try {
            WSDLReader reader = factory.newReader();
            Definition definition = reader.readWSDL(wsdlLocation.toURI().toString());
            if (!definition.getNamespaces().values().contains("http://schemas.xmlsoap.org/wsdl/soap/")) {
                // Workaround for a bug in WSDL4J where a WSDL document does not reference the SOAP namespace and an attempt is made to serialize it,
                // an exception is thrown. 
                definition.addNamespace("soap11", "http://schemas.xmlsoap.org/wsdl/soap/");
            }
            return definition;
        } catch (WSDLException e) {
            throw new InstallException(e);
        } catch (URISyntaxException e) {
            throw new InstallException(e);
        }
    }

    private XmlSchemaCollection parseSchema(Definition definition) {
        XmlSchemaCollection collection = new XmlSchemaCollection();
        Types types = definition.getTypes();
        if (types == null) {
            // types not defined
            return collection;
        }
        for (Object obj : types.getExtensibilityElements()) {
            if (obj instanceof Schema) {
                Schema schema = (Schema) obj;
                Element element = schema.getElement();
                collection.setBaseUri(schema.getDocumentBaseURI());
                // create a synthetic id to work around issue where XmlSchema cannot handle elements with the same targetnamespace
                String syntheticId = UUID.randomUUID().toString();
                collection.read(element, syntheticId);
            }

        }
        return collection;
    }

}