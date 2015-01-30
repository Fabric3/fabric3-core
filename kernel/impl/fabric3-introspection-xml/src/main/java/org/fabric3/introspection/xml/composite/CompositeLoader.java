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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.xml.composite;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.contribution.ArtifactValidationFailure;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.PropertyValue;
import org.fabric3.api.model.type.component.Resource;
import org.fabric3.api.model.type.component.Wire;
import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.fabric3.spi.introspection.xml.CompositeConstants.CHANNEL;
import static org.fabric3.spi.introspection.xml.CompositeConstants.COMPONENT;
import static org.fabric3.spi.introspection.xml.CompositeConstants.COMPOSITE;
import static org.fabric3.spi.introspection.xml.CompositeConstants.INCLUDE;
import static org.fabric3.spi.introspection.xml.CompositeConstants.WIRE;

/**
 * Loads a composite component definition from an XML-based assembly file
 */
@EagerInit
public class CompositeLoader extends AbstractExtensibleTypeLoader<Composite> {

    /**
     * Constructor. Specific loaders to handle overloaded <code>property>, <code>service</code> and <code>reference</code> elements on composites and
     * components.
     *
     * @param registry the loader registry to register with; also used to load extension elements
     */
    @Constructor
    public CompositeLoader(@Reference LoaderRegistry registry) {
        super(registry);
        addAttributes("name",
                      "autowire",
                      "targetNamespace",
                      "local",
                      "requires",
                      "policySets",
                      "constrainingType",
                      "channel",
                      "schemaLocation",
                      "deployable",
                      "modes",
                      "environments");
    }

    public QName getXMLType() {
        return COMPOSITE;
    }

    @SuppressWarnings({"VariableNotUsedInsideIf"})
    public Composite load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        // track locations so they can be used to report validation errors after the parser has been advanced
        Map<ModelObject, Location> locations = new HashMap<>();

        String name = reader.getAttributeValue(null, "name");
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
        IntrospectionContext childContext = new DefaultIntrospectionContext(context, targetNamespace);
        QName compositeName = new QName(targetNamespace, name);
        NamespaceContext nsContext = createNamespaceContext(reader);

        Composite type = new Composite(compositeName);
        String deployable = reader.getAttributeValue(null, "deployable");
        type.setDeployable(Boolean.parseBoolean(deployable));
        String modes = reader.getAttributeValue(null, "modes");
        String environments = reader.getAttributeValue(null, "environments");
        if (environments != null) {
            type.setEnvironments(Arrays.asList(environments.split(" ")));
        }
        parseModes(modes, type, reader, context);

        type.setContributionUri(context.getContributionUri());

        validateAttributes(reader, context, type);

        while (true) {
            int val = reader.next();
            switch (val) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (INCLUDE.equals(qname)) {
                        handleInclude(type, reader, locations, compositeName, childContext, context);
                        continue;
                    } else if (CHANNEL.equals(qname)) {
                        handleChannel(type, reader, locations, compositeName, childContext, context);
                        continue;
                    } else if (COMPONENT.equals(qname)) {
                        boolean valid = handleComponent(type, reader, nsContext, locations, compositeName, childContext, context);
                        if (!valid) {
                            updateContext(context, childContext, compositeName);
                        }
                        continue;
                    } else if (WIRE.equals(qname)) {
                        handleWire(type, reader, compositeName, childContext, context);
                        continue;
                    } else {
                        handleExtensionElement(type, reader, childContext);
                        continue;
                    }
                case END_ELEMENT:
                    if (!COMPOSITE.equals(reader.getName())) {
                        continue;
                    }
                    updateContext(context, childContext, compositeName);
                    return type;
            }

        }
    }

    private void handleExtensionElement(Composite type, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        // Extension element - for now try to load and see if we can handle it
        ModelObject modelObject = registry.load(reader, ModelObject.class, context);
        // TODO when the loader registry is replaced this try..catch must be replaced with a check for a loader and an
        // UnrecognizedElement added to the context if none is found
        if (modelObject instanceof Property) {
            type.add((Property) modelObject);
        } else if (modelObject instanceof Component) {
            type.add((Component<?>) modelObject);
        } else if (modelObject instanceof Resource) {
            type.add((Resource) modelObject);
        } else if (modelObject == null) {
            // loaders may elect to return a null element; ignore
        } else {
            UnrecognizedElement failure = new UnrecognizedElement(reader, startLocation, type);
            context.addError(failure);
        }
    }

    private void handleWire(Composite type, XMLStreamReader reader, QName compositeName, IntrospectionContext context, IntrospectionContext parentContext)
            throws XMLStreamException {
        Wire wire;
        wire = registry.load(reader, Wire.class, context);
        if (wire == null) {
            // error encountered loading the wire
            updateContext(parentContext, context, compositeName);
            return;
        }
        type.add(wire);
    }

    private boolean handleComponent(Composite type,
                                    XMLStreamReader reader,
                                    NamespaceContext nsContext,
                                    Map<ModelObject, Location> locations,
                                    QName compositeName,
                                    IntrospectionContext context,
                                    IntrospectionContext parentContext) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        Component<?> component = registry.load(reader, Component.class, context);
        if (component == null) {
            // error encountered loading the componentDefinition
            updateContext(parentContext, context, compositeName);
            return false;
        }
        String key = component.getName();
        if (type.getComponents().containsKey(key)) {
            DuplicateComponentName failure = new DuplicateComponentName(key, startLocation, type);
            context.addError(failure);
            return false;
        }
        type.add(component);
        if (component.getComponentType() == null) {
            return false;
        }

        // Calculate the namespace context from the composite element since XMLStreamReader.getNamespaceCount() only returns the number of namespaces
        // declared on the current element. This means namespaces defined on parent elements which are active (e.g. <composite>) or not reported.
        // Scoping results in no namespaces being reported 
        for (PropertyValue value : component.getPropertyValues().values()) {
            value.setNamespaceContext(nsContext);
        }
        locations.put(component, startLocation);
        return true;
    }

    private void handleChannel(Composite type,
                               XMLStreamReader reader,
                               Map<ModelObject, Location> locations,
                               QName compositeName,
                               IntrospectionContext context,
                               IntrospectionContext parentContext) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        Channel channel = registry.load(reader, Channel.class, context);
        if (channel == null) {
            // error encountered loading the channel definition
            updateContext(parentContext, context, compositeName);
            return;
        }
        String key = channel.getName();
        if (type.getChannels().containsKey(key)) {
            DuplicateChannelName failure = new DuplicateChannelName(key, startLocation, type);
            context.addError(failure);
            return;
        }
        locations.put(channel, startLocation);
        type.add(channel);
    }

    private void handleInclude(Composite type,
                               XMLStreamReader reader,
                               Map<ModelObject, Location> locations,
                               QName compositeName,
                               IntrospectionContext context,
                               IntrospectionContext parentContext) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        Include include = registry.load(reader, Include.class, context);
        if (include == null) {
            // error encountered loading the include
            updateContext(parentContext, context, compositeName);
            return;
        }
        QName includeName = include.getName();
        if (type.getIncludes().containsKey(includeName)) {
            String identifier = includeName.toString();
            DuplicateInclude failure = new DuplicateInclude(identifier, startLocation, include);
            context.addError(failure);
            return;
        }
        Composite included = include.getIncluded();
        if (included == null) {
            return;
        }
        for (Component definition : included.getComponents().values()) {
            String key = definition.getName();
            if (type.getComponents().containsKey(key)) {
                DuplicateComponentName failure = new DuplicateComponentName(key, startLocation, type);
                context.addError(failure);
            }
        }
        locations.put(include, startLocation);
        type.add(include);
    }

    private void updateContext(IntrospectionContext context, IntrospectionContext childContext, QName compositeName) {
        if (childContext.hasErrors() || childContext.hasWarnings()) {
            URI uri = context.getContributionUri();
            if (childContext.hasErrors()) {
                ArtifactValidationFailure artifactFailure = new ArtifactValidationFailure(uri, compositeName.toString());
                artifactFailure.addFailures(childContext.getErrors());
                context.addError(artifactFailure);
            }
            if (childContext.hasWarnings()) {
                ArtifactValidationFailure artifactFailure = new ArtifactValidationFailure(uri, compositeName.toString());
                artifactFailure.addFailures(childContext.getWarnings());
                context.addWarning(artifactFailure);
            }
        }
    }

    private NamespaceContext createNamespaceContext(XMLStreamReader reader) {
        StatefulNamespaceContext namespaceContext = new StatefulNamespaceContext();
        int count = reader.getNamespaceCount();
        for (int i = 0; i < count; i++) {
            String prefix = reader.getNamespacePrefix(i);
            if (prefix == null) {
                continue;
            }
            String namespaceUri = reader.getNamespaceURI(prefix);
            namespaceContext.addNamespace(prefix, namespaceUri);
        }
        return namespaceContext;
    }

    private void parseModes(String modes, Composite type, XMLStreamReader reader, IntrospectionContext context) {
        if (modes != null) {
            String[] tokens = modes.split(" ");
            List<RuntimeMode> runtimeModes = new ArrayList<>();
            for (String token : tokens) {
                try {
                    runtimeModes.add(RuntimeMode.valueOf(token.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    context.addError(new InvalidValue("Invalid runtime mode: " + token, reader.getLocation(), type));
                }
            }
            type.setModes(runtimeModes);
        }
    }

}
