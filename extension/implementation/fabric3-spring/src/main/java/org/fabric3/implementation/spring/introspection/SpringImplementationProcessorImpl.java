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
package org.fabric3.implementation.spring.introspection;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.fabric3.api.host.stream.Source;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.implementation.spring.model.BeanDefinition;
import org.fabric3.implementation.spring.model.SpringComponentType;
import org.fabric3.implementation.spring.model.SpringConsumer;
import org.fabric3.implementation.spring.model.SpringReference;
import org.fabric3.implementation.spring.model.SpringService;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.model.type.java.JavaType;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Remotable;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Default SpringImplementationProcessor implementation.
 */
public class SpringImplementationProcessorImpl implements SpringImplementationProcessor {
    private static final String BEAN = "bean";
    private static final QName SERVICE = new QName(Constants.SCA_NS, "service");
    private static final QName REFERENCE = new QName(Constants.SCA_NS, "reference");
    private static final QName PROPERTY = new QName(Constants.SCA_NS, "property");
    private static final QName PRODUCER = new QName(Constants.SCA_NS, "producer");
    private static final QName CONSUMER = new QName(Constants.SCA_NS, "consumer");

    private static final String XSD_NS = XMLConstants.W3C_XML_SCHEMA_NS_URI;
    private static final QName XSD_STRING = new QName(XSD_NS, "string");
    private static final QName XSD_BOOLEAN = new QName(XSD_NS, "boolean");
    private static final QName XSD_INT = new QName(XSD_NS, "integer");

    private JavaContractProcessor contractProcessor;
    private final XMLInputFactory xmlInputFactory;

    private boolean strictValidation;

    @org.oasisopen.sca.annotation.Property(required = false)
    public void setValidate(boolean validation) {
        strictValidation = validation;
    }

    public SpringImplementationProcessorImpl(@org.oasisopen.sca.annotation.Reference JavaContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
        xmlInputFactory = XMLInputFactory.newFactory();
    }

    public SpringComponentType introspect(Source source, IntrospectionContext context) throws XMLStreamException {
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            SpringComponentType type = new SpringComponentType();
            stream = source.openStream();
            reader = xmlInputFactory.createXMLStreamReader(stream);
            Location start = reader.getLocation();

            processStream(context, reader, type);

            if (source instanceof MultiSource) {
                // multiple app contexts
                MultiSource multiSource = (MultiSource) source;
                for (Source contextSource : multiSource.getSources()) {
                    stream = contextSource.openStream();
                    reader = xmlInputFactory.createXMLStreamReader(stream);
                    processStream(context, reader, type);
                }
            }
            validate(type, context, start);

            return type;
        } finally {
            if (reader != null) {
                reader.close();
            }
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // ignore
                e.printStackTrace();
            }
        }
    }

    private void processStream(IntrospectionContext context, XMLStreamReader reader, SpringComponentType type) throws XMLStreamException {
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (BEAN.equals(reader.getName().getLocalPart())) {
                        if (!processBean(type, reader, context)) {
                            return;
                        }
                    } else if (SERVICE.equals(reader.getName())) {
                        if (!processService(type, reader, context)) {
                            return;
                        }
                    } else if (REFERENCE.equals(reader.getName())) {
                        if (!processReference(type, reader, context)) {
                            return;
                        }
                    } else if (PROPERTY.equals(reader.getName())) {
                        if (!processProperty(type, reader, context)) {
                            return;
                        }
                    } else if (PRODUCER.equals(reader.getName())) {
                        if (!processProducer(type, reader, context)) {
                            return;
                        }
                    } else if (CONSUMER.equals(reader.getName())) {
                        if (!processConsumer(type, reader, context)) {
                            return;
                        }
                    } else {
                        if (reader.getName().getNamespaceURI().equals(Constants.SCA_NS)) {
                            UnrecognizedElement error = new UnrecognizedElement(reader, reader.getLocation(), type);
                            context.addError(error);
                        }
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    postProcess(type, context);
                    return;
            }
        }
    }

    /**
     * Performs validation.
     *
     * @param type    the component type
     * @param context the context
     */
    private void validate(SpringComponentType type, IntrospectionContext context, Location location) {
        Collection<Reference<ComponentType>> references = type.getReferences().values();
        for (Reference reference : references) {
            String defaultStr = ((SpringReference) reference).getDefaultValue();
            if (defaultStr != null) {
                if (!type.getBeansById().containsKey(defaultStr) && !type.getBeansByName().containsKey(defaultStr)) {
                    InvalidValue error = new InvalidValue("Default value '" + defaultStr + "' does not reference a valid bean", location, type);
                    context.addError(error);
                }
            }
        }

        if (strictValidation) {
            // SCA spec validation
            if (type.getSpringServices().isEmpty()) {
                // if no services defined, check remotables
                for (BeanDefinition beanDefinition : type.getBeansByName().values()) {
                    validateBean(type, beanDefinition, context, location);
                }
                for (BeanDefinition beanDefinition : type.getBeansById().values()) {
                    validateBean(type, beanDefinition, context, location);
                }
            }
        }
    }

    private void validateBean(SpringComponentType type, BeanDefinition beanDefinition, IntrospectionContext context, Location location) {
        Class<?> clazz = beanDefinition.getBeanClass();
        int number = 0;
        for (Class<?> interfaze : clazz.getInterfaces()) {
            if (interfaze.isAnnotationPresent(Remotable.class)) {
                number++;
            }
        }
        if (number > 1) {
            InvalidValue error = new InvalidValue("Bean cannot implement multiple remotable services if no SCA services are defined in the parent" +
                                                  " application context: " + clazz, location, type);
            context.addError(error);
        }
    }

    /**
     * Processes a Spring <code>bean</code> definition.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processBean(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        String id = reader.getAttributeValue(null, "id");
        String name = reader.getAttributeValue(null, "name");
        if (id == null && name == null) {
            MissingAttribute failure = new MissingAttribute("A bean id or name must be specified", location);
            context.addError(failure);
            return false;
        }
        String classAttr = reader.getAttributeValue(null, "class");
        Class<?> clazz = null;
        if (classAttr != null) {
            try {
                clazz = context.getClassLoader().loadClass(classAttr);
            } catch (ClassNotFoundException e) {
                InvalidValue failure = new InvalidValue("Bean class not found: " + classAttr, location, e);
                context.addError(failure);
            }
        }
        BeanDefinition bean = new BeanDefinition();
        bean.setId(id);
        bean.setName(name);
        bean.setBeanClass(clazz);
        type.add(bean);

        return true;
    }

    /**
     * Processes an SCA <code>service</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processService(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        // TODO This does not currently support policy declarations
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A service name must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        if (type.getServices().containsKey(name)) {
            DuplicateService failure = new DuplicateService(name, startLocation, type);
            context.addError(failure);
            return false;
        }
        String target = reader.getAttributeValue(null, "target");
        if (target == null) {
            MissingAttribute failure = new MissingAttribute("A service target must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        String typeAttr = reader.getAttributeValue(null, "type");
        ServiceContract contract = null;
        if (typeAttr != null) {
            Class<?> interfaze;
            try {
                ClassLoader loader = context.getClassLoader();
                interfaze = loader.loadClass(typeAttr);
            } catch (ClassNotFoundException e) {
                InvalidValue failure = new InvalidValue("Service interface not found: " + typeAttr, startLocation);
                context.addError(failure);
                return false;
            }
            contract = contractProcessor.introspect(interfaze, context, type);
        }
        SpringService definition = new SpringService(name, contract, target);
        type.add(definition);
        return true;
    }

    /**
     * Processes an SCA <code>reference</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processReference(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        // TODO This does not currently support policy declarations
        // TODO This does not currently support the @default attribute
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A reference name must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        if (type.getReferences().containsKey(name)) {
            DuplicateReference failure = new DuplicateReference(name, startLocation, type);
            context.addError(failure);
            return false;
        }
        String typeAttr = reader.getAttributeValue(null, "type");
        if (typeAttr == null) {
            MissingAttribute failure = new MissingAttribute("A service type must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        Class<?> interfaze;
        try {
            ClassLoader loader = context.getClassLoader();
            interfaze = loader.loadClass(typeAttr);
        } catch (ClassNotFoundException e) {
            InvalidValue failure = new InvalidValue("Service interface not found: " + typeAttr, startLocation);
            context.addError(failure);
            return false;
        }
        String defaultStr = reader.getAttributeValue(null, "default");

        ServiceContract contract = contractProcessor.introspect(interfaze, context, type);
        Reference definition = new SpringReference(name, contract, defaultStr);
        type.add(definition);
        return true;
    }

    /**
     * Processes an SCA <code>property</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processProperty(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A property name must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        if (type.getProperties().containsKey(name)) {
            DuplicateProperty failure = new DuplicateProperty(name, startLocation, type);
            context.addError(failure);
            return false;
        }

        Property property = new Property(name);
        property.setRequired(true);

        String propertyType = reader.getAttributeValue(null, "type");

        if (Integer.class.getName().equals(propertyType)) {
            property.setType(XSD_INT);
        } else if (Boolean.class.getName().equals(propertyType)) {
            property.setType(XSD_BOOLEAN);
        } else {
            property.setType(XSD_STRING);
        }

        type.add(property);
        return true;
    }

    /**
     * Processes an SCA <code>consumer</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private <T> boolean processConsumer(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A consumer name must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        if (type.getConsumers().containsKey(name)) {
            DuplicateConsumer failure = new DuplicateConsumer(name, startLocation, type);
            context.addError(failure);
            return false;
        }
        String typeAttr = reader.getAttributeValue(null, "type");
        if (typeAttr == null) {
            MissingAttribute failure = new MissingAttribute("A consumer data type must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        Class<T> consumerType;
        try {
            ClassLoader loader = context.getClassLoader();
            consumerType = cast(loader.loadClass(typeAttr));
        } catch (ClassNotFoundException e) {
            InvalidValue failure = new InvalidValue("Consumer interface not found: " + typeAttr, startLocation);
            context.addError(failure);
            return false;
        }
        JavaType dataType = new JavaType(consumerType);
        String target = reader.getAttributeValue(null, "target");
        if (target == null) {
            MissingAttribute failure = new MissingAttribute("A consumer target must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        String[] targetTokens = target.split("/");
        if (targetTokens.length != 2) {
            InvalidValue failure = new InvalidValue("Target value must be in the form beanName/methodName", startLocation);
            context.addError(failure);
            return false;
        }
        Consumer definition = new SpringConsumer(name, dataType, targetTokens[0], targetTokens[1]);
        type.add(definition);
        return true;
    }

    /**
     * Processes an SCA <code>producer</code> element.
     *
     * @param type    the component type
     * @param reader  the reader
     * @param context the context for reporting errors
     * @return true if processing completed without validation errors
     */
    private boolean processProducer(SpringComponentType type, XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("A producer name must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        if (type.getProducers().containsKey(name)) {
            DuplicateProducer failure = new DuplicateProducer(name, startLocation, type);
            context.addError(failure);
            return false;
        }

        String typeAttr = reader.getAttributeValue(null, "type");
        if (typeAttr == null) {
            MissingAttribute failure = new MissingAttribute("A producer data type must be specified", startLocation);
            context.addError(failure);
            return false;
        }
        Class<?> interfaze;
        try {
            ClassLoader loader = context.getClassLoader();
            interfaze = loader.loadClass(typeAttr);
        } catch (ClassNotFoundException e) {
            InvalidValue failure = new InvalidValue("Service interface not found: " + typeAttr, startLocation);
            context.addError(failure);
            return false;
        }
        ServiceContract contract = contractProcessor.introspect(interfaze, context, type);
        if (contract.getOperations().size() != 1) {
            String interfaceName = contract.getInterfaceName();
            InvalidValue error = new InvalidValue("Producer interfaces must have one method: " + interfaceName, startLocation, type);
            context.addError(error);
        }

        Producer definition = new Producer(name, contract);
        type.add(definition);
        return true;
    }

    /**
     * Performs heuristic introspection and validation.
     *
     * @param type    the component type
     * @param context the context for reporting errors
     */
    private void postProcess(SpringComponentType type, IntrospectionContext context) {
        if (type.getServices().isEmpty() && type.getReferences().isEmpty() && type.getProperties().isEmpty()) {
            processHueristics(type, context);
            return;
        }
        // introspect service contracts for service elements that do not explicitly have a type element
        postProcessServices(type, context);
    }

    /**
     * Performs heuristic introspection.
     *
     * @param type    the component type
     * @param context the context for reporting errors
     */
    private void processHueristics(SpringComponentType type, IntrospectionContext context) {
        // TODO synthesize optional references
        // TODO synthesize services
        // TODO synthesize properties
    }

    /**
     * Performs heuristic introspection and validation of services.
     *
     * @param type    the component type
     * @param context the context for reporting errors
     */
    private void postProcessServices(SpringComponentType type, IntrospectionContext context) {
        for (SpringService service : type.getSpringServices().values()) {
            String target = service.getTarget();
            BeanDefinition definition = type.getBeansById().get(target);
            if (definition == null) {
                definition = type.getBeansByName().get(target);
            }
            if (definition == null) {
                ServiceTargetNotFound failure = new ServiceTargetNotFound(service.getName(), target, type);
                context.addError(failure);
                continue;
            }
            if (service.getServiceContract() == null) {
                introspectContract(service, definition, type, context);
            }
        }
    }

    /**
     * Introspects a service contract from a bean definition.
     *
     * @param service    the service
     * @param definition the bean definition
     * @param type       the component type
     * @param context    the context for reporting errors
     */
    private void introspectContract(SpringService service, BeanDefinition definition, SpringComponentType type, IntrospectionContext context) {
        Class<?> beanClass = definition.getBeanClass();
        String serviceName = service.getName();
        if (beanClass == null) {
            UnknownServiceType failure = new UnknownServiceType(serviceName, beanClass, type);
            context.addError(failure);
            return;
        }
        Class<?>[] interfaces = beanClass.getInterfaces();
        if (interfaces.length == 0) {
            // use the implementation class
            ServiceContract contract = contractProcessor.introspect(beanClass, context, type);
            service.setServiceContract(contract);
        } else if (interfaces.length == 1) {
            // default service
            ServiceContract contract = contractProcessor.introspect(interfaces[0], context, type);
            service.setServiceContract(contract);
        } else {
            // match on service name
            ServiceContract contract = null;
            for (Class<?> interfaze : interfaces) {
                if (serviceName.equals(interfaze.getSimpleName())) {
                    contract = contractProcessor.introspect(interfaze, context, type);
                    service.setServiceContract(contract);
                    break;
                }
            }
            if (contract == null) {
                UnknownServiceType failure = new UnknownServiceType(serviceName, beanClass, type);
                context.addError(failure);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }

}
