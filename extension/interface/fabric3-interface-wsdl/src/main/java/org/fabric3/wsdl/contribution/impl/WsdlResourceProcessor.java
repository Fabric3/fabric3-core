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
*/
package org.fabric3.wsdl.contribution.impl;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaException;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.api.host.stream.Source;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.wsdl.contribution.BindingSymbol;
import org.fabric3.wsdl.contribution.PortSymbol;
import org.fabric3.wsdl.contribution.PortTypeSymbol;
import org.fabric3.wsdl.contribution.ServiceSymbol;
import org.fabric3.wsdl.contribution.WsdlResourceProcessorExtension;
import org.fabric3.wsdl.contribution.WsdlServiceContractSymbol;
import org.fabric3.wsdl.contribution.WsdlSymbol;
import org.fabric3.wsdl.factory.Wsdl4JFactory;
import org.fabric3.wsdl.model.WsdlServiceContract;
import org.fabric3.wsdl.processor.WsdlContractProcessor;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Indexes and processes a WSDL document, referenced schemas, and introspected service contracts deriving from port types in a contribution. This implementation
 * uses the WSDL4J to represent the document.
 */
@EagerInit
public class WsdlResourceProcessor implements ResourceProcessor {
    private static final QName SCHEMA_NAME = new QName(W3C_XML_SCHEMA_NS_URI, "schema");
    private static final QName IMPORT_NAME = new QName(W3C_XML_SCHEMA_NS_URI, "import");
    private static final QName SCHEMA_LOCATION = new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
    private static final QName CALLBACK_ATTRIBUTE = new QName(Constants.SCA_NS, "callback");
    private static final String MIME_TYPE = "text/wsdl+xml";

    private ProcessorRegistry registry;
    private WsdlContractProcessor processor;
    private MetaDataStore store;
    private Wsdl4JFactory factory;
    private DocumentBuilderFactory documentBuilderFactory;
    private List<WsdlResourceProcessorExtension> extensions = new ArrayList<>();

    public WsdlResourceProcessor(@Reference ProcessorRegistry registry,
                                 @Reference WsdlContractProcessor processor,
                                 @Reference MetaDataStore store,
                                 @Reference Wsdl4JFactory factory) {
        this.registry = registry;
        this.processor = processor;
        this.store = store;
        this.factory = factory;
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
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

    public void index(Resource resource, IntrospectionContext context) throws InstallException {
        InputStream stream = null;
        try {
            Source source = resource.getSource();
            stream = source.openStream();
            // eagerly process the WSDL since port types need to be available during contribution processing.
            parse(resource, context);
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
        Definition definition = resolveLocalWsdl(resource);
        // Process callbacks here (as opposed to eagerly in #index(..) since the SCA callback attribute may reference a portType in another document.
        // Processing at this point guarantees the callback portType will be indexed and referenceable.
        for (ResourceElement<?, ?> element : resource.getResourceElements()) {
            if (element.getSymbol() instanceof WsdlServiceContractSymbol) {
                WsdlServiceContract contract = (WsdlServiceContract) element.getValue();
                PortType portType = contract.getPortType();
                QName callbackPortTypeName = (QName) portType.getExtensionAttribute(CALLBACK_ATTRIBUTE);
                if (callbackPortTypeName != null) {
                    PortType callbackPortType = definition.getPortType(callbackPortTypeName);
                    if (callbackPortType != null) {
                        resolveLocalCallbackContract(callbackPortTypeName, contract, resource, context);
                    } else {
                        resolveExternalCallbackContract(callbackPortTypeName, contract, context);
                    }
                }
            }
        }
        resource.setState(ResourceState.PROCESSED);
    }

    /**
     * Parse all WSDL-related elements in a resource pointing to a WSDL document.
     *
     * @param resource the resource
     * @param context  introspection context
     * @throws InstallException if an unexpected error occurs
     */
    @SuppressWarnings({"unchecked"})
    private void parse(Resource resource, IntrospectionContext context) throws InstallException {
        // parse the WSDL
        Source source = resource.getSource();
        Definition definition = parseWsdl(source, context);
        QName wsdlQName = definition.getQName();
        WsdlSymbol wsdlSymbol = new WsdlSymbol(wsdlQName);
        ResourceElement<WsdlSymbol, Definition> wsdlElement = new ResourceElement<>(wsdlSymbol, definition);
        resource.addResourceElement(wsdlElement);

        Map<String, Service> services = definition.getServices();
        for (Service service : services.values()) {
            ServiceSymbol serviceSymbol = new ServiceSymbol(service.getQName());
            ResourceElement<ServiceSymbol, Service> serviceElement = new ResourceElement<>(serviceSymbol, service);
            resource.addResourceElement(serviceElement);
            Map<String, Port> ports = service.getPorts();
            for (Port port : ports.values()) {
                QName portName = new QName(definition.getTargetNamespace(), port.getName());
                PortSymbol portSymbol = new PortSymbol(portName);
                ResourceElement<PortSymbol, Port> portElement = new ResourceElement<>(portSymbol, port);
                resource.addResourceElement(portElement);
            }
        }
        for (Object object : definition.getPortTypes().values()) {
            PortType portType = (PortType) object;
            QName name = portType.getQName();
            PortTypeSymbol symbol = new PortTypeSymbol(name);
            ResourceElement<PortTypeSymbol, PortType> element = new ResourceElement<>(symbol, portType);
            resource.addResourceElement(element);
        }
        // parse WSDL schemas
        XmlSchemaCollection schemaCollection = parseSchema(definition, context);
        // TODO index XML schema elements and types?

        // introspect port type service contracts
        for (Object object : definition.getPortTypes().values()) {
            PortType portType = (PortType) object;
            WsdlServiceContract contract = processor.introspect(portType, definition, schemaCollection, context);
            QName name = portType.getQName();
            WsdlServiceContractSymbol symbol = new WsdlServiceContractSymbol(name);
            ResourceElement<WsdlServiceContractSymbol, WsdlServiceContract> element
                    = new ResourceElement<>(symbol, contract);
            resource.addResourceElement(element);
        }

        // introspect bindings
        Collection<Binding> bindings = definition.getBindings().values();
        for (Binding binding : bindings) {
            BindingSymbol bindingSymbol = new BindingSymbol(binding.getQName());
            ResourceElement<BindingSymbol, Binding> serviceElement = new ResourceElement<>(bindingSymbol, binding);
            resource.addResourceElement(serviceElement);
        }

        // callback processor extensions
        for (WsdlResourceProcessorExtension extension : extensions) {
            extension.process(resource, definition);
        }
    }

    /**
     * Parses the WSDL document.
     *
     * @param source  the Source for reading the document
     * @param context the introspection context
     * @return the parsed WSDL
     * @throws InstallException if an unexpected error occurs
     */
    private Definition parseWsdl(Source source, IntrospectionContext context) throws InstallException {
        WSDLLocator locator = new SourceWsdlLocator(source, context);
        try {
            WSDLReader reader = factory.newReader();
            Definition definition = reader.readWSDL(locator);
            if (!definition.getNamespaces().values().contains("http://schemas.xmlsoap.org/wsdl/soap/")) {
                // Workaround for a bug in WSDL4J where a WSDL document does not reference the SOAP namespace and an attempt is made to serialize it,
                // an exception is thrown. 
                definition.addNamespace("soap11", "http://schemas.xmlsoap.org/wsdl/soap/");
            }
            parseSchemaLocation(definition, context);
            return definition;
        } catch (WSDLException e) {
            throw new InstallException(e);
        } finally {
            locator.close();
        }
    }

    /**
     * Parses the schemaLocation attribute if present.
     *
     * @param definition the WSDL
     * @param context    the introspection context
     * @throws InstallException if an unexpected error parsing the schema occurs
     */
    private void parseSchemaLocation(Definition definition, IntrospectionContext context) throws InstallException {
        QName schemaLocation = (QName) definition.getExtensionAttribute(SCHEMA_LOCATION);
        if (schemaLocation == null) {
            // no schema location present, skip
            return;
        }
        // strip extra whitespace
        String trimmed = schemaLocation.getLocalPart().replaceAll("\\s{2,}", " ");
        String[] locationValue = trimmed.split(" ");
        int len = locationValue.length;
        if (len == 1) {
            populateSchemaTypes(definition, "", locationValue[0]);
        } else if (len > 1) {
            if (len % 2 != 0) {
                // make sure the schema location contains pairs of values if more than one
                InvalidSchemaLocation error = new InvalidSchemaLocation("Invalid schemaLocation value: " + schemaLocation.getLocalPart());
                context.addError(error);
                return;
            }
            for (int i = 0; i < locationValue.length - 1; i++) {
                String namespace = locationValue[i];
                String location = locationValue[i + 1];
                populateSchemaTypes(definition, namespace, location);
            }
        }
    }

    /**
     * Populate the WSDL with an import for the schema location value.
     *
     * @param definition      the WSDL
     * @param targetNamespace the schema target namespace
     * @param schemaLocation  the de-referenceable schema location
     * @throws InstallException if an unexpected error parsing the schema occurs
     */
    private void populateSchemaTypes(Definition definition, String targetNamespace, String schemaLocation) throws InstallException {
        Types types = definition.createTypes();
        Schema schema;
        try {
            schema = (Schema) definition.getExtensionRegistry().createExtension(Types.class, SCHEMA_NAME);
        } catch (WSDLException e) {
            throw new InstallException(e);
        }
        Document document;
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new InstallException(e);
        }
        Element schemaElement = document.createElementNS(SCHEMA_NAME.getNamespaceURI(), "xsd:schema");
        schema.setElement(schemaElement);
        Element importElement = document.createElementNS(IMPORT_NAME.getNamespaceURI(), "xsd:import");
        importElement.setAttribute("namespace", targetNamespace);
        importElement.setAttribute("schemaLocation", schemaLocation);
        schemaElement.appendChild(importElement);
        types.addExtensibilityElement(schema);
        definition.setTypes(types);
    }

    /**
     * Parses the contents of schema entries and imported documents using Apache Commons XmlSchema.
     *
     * @param definition the WSDL
     * @param context    the introspection context
     * @return the parsed schema values
     */
    private XmlSchemaCollection parseSchema(Definition definition, IntrospectionContext context) {
        XmlSchemaCollection collection = new XmlSchemaCollection();
        collection.setSchemaResolver(createResolvers(collection));
        try {
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
                    // create a synthetic id to work around issue where XmlSchema cannot handle elements with the same target namespace
                    String syntheticId = definition.getDocumentBaseURI() + "#" + UUID.randomUUID().toString();
                    collection.read(element, syntheticId);
                }

            }
        } catch (RuntimeException e) {
            // For some reason, Apache XmlSchema wraps schema exceptions in a generic RuntimeException. Check if that is the case. 
            if (!(e.getCause() instanceof XmlSchemaException)) {
                throw e;
            }
            InvalidWsdl error = new InvalidWsdl("Error parsing Schema", e.getCause());
            context.addError(error);
        }
        return collection;
    }

    private URIResolver createResolvers(XmlSchemaCollection collection) {
        DefaultURIResolver defaultResolver = new DefaultURIResolver();
        ContextClassLoaderResolver classLoaderResolver = new ContextClassLoaderResolver(defaultResolver);
        return new RelativeUrlResolver(collection, classLoaderResolver);
    }

    private Definition resolveLocalWsdl(Resource resource) {
        Definition definition = null;
        for (ResourceElement<?, ?> element : resource.getResourceElements()) {
            if (element.getSymbol() instanceof WsdlSymbol) {
                definition = (Definition) element.getValue();
                break;
            }
        }
        if (definition == null) {
            // should not happen
            throw new AssertionError("WSDL document not found");
        }
        return definition;
    }

    private void resolveExternalCallbackContract(QName callbackPortTypeName, WsdlServiceContract contract, IntrospectionContext context) {

        WsdlServiceContractSymbol symbol = new WsdlServiceContractSymbol(callbackPortTypeName);
        URI contributionUri = context.getContributionUri();
        ResourceElement<WsdlServiceContractSymbol, WsdlServiceContract> resolved;
        try {
            resolved = store.resolve(contributionUri, WsdlServiceContract.class, symbol, context);
        } catch (StoreException e) {
            CallbackContractLoadError error = new CallbackContractLoadError("Error resolving callback port type:" + callbackPortTypeName, e);
            context.addError(error);
            return;
        }
        if (resolved == null) {
            PortTypeNotFound error = new PortTypeNotFound("Callback port type not found: " + callbackPortTypeName);
            context.addError(error);
            return;

        }
        WsdlServiceContract callbackContract = resolved.getValue();
        contract.setCallbackContract(callbackContract);
    }

    private void resolveLocalCallbackContract(QName callbackPortTypeName, WsdlServiceContract contract, Resource resource, IntrospectionContext context) {

        WsdlServiceContractSymbol symbol = new WsdlServiceContractSymbol(callbackPortTypeName);

        for (ResourceElement<?, ?> resourceElement : resource.getResourceElements()) {
            if (resourceElement.getSymbol().equals(symbol)) {
                WsdlServiceContract callbackContract = (WsdlServiceContract) resourceElement.getValue();
                contract.setCallbackContract(callbackContract);
                break;
            }
        }
        if (contract.getCallbackContract() == null) {
            PortTypeNotFound error = new PortTypeNotFound("Callback port type not found: " + callbackPortTypeName);
            context.addError(error);
        }
    }

}