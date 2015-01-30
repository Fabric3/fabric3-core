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
package org.fabric3.api.model.type.builder;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.ResourceDefinition;
import org.fabric3.api.model.type.component.WireDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Builds {@link Composite}s.
 */
public class CompositeBuilder extends AbstractBuilder {
    private static final DocumentBuilderFactory DOCUMENT_FACTORY;

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
    }

    private Composite composite;

    /**
     * Creates a new builder using the given composite name.
     *
     * @param name the composite name
     * @return the builder
     */
    public static CompositeBuilder newBuilder(QName name) {
        return new CompositeBuilder(name);
    }

    /**
     * Adds a component definition to the composite.
     *
     * @param definition the component definition
     * @return the builder
     */
    public CompositeBuilder component(ComponentDefinition<?> definition) {
        checkState();
        composite.add(definition);
        return this;
    }

    /**
     * Adds a channel definition to the composite.
     *
     * @param definition the channel definition
     * @return the builder
     */
    public CompositeBuilder channel(ChannelDefinition definition) {
        checkState();
        composite.add(definition);
        return this;
    }

    /**
     * Adds a resource definition to the composite.
     *
     * @param definition the resource definition
     * @return the builder
     */
    public CompositeBuilder resource(ResourceDefinition definition) {
        checkState();
        composite.add(definition);
        return this;
    }

    /**
     * Includes the composite in the current composite.
     *
     * @param included the name of the composite to include
     * @return the builder
     */
    public CompositeBuilder include(Composite included) {
        checkState();
        Include include = new Include();
        include.setIncluded(included);
        include.setName(included.getName());
        composite.add(include);
        return this;
    }

    /**
     * Adds the wire definition to the composite.
     *
     * @param wireDefinition the wire definition
     * @return the builder
     */
    public CompositeBuilder wire(WireDefinition wireDefinition) {
        checkState();
        composite.add(wireDefinition);
        return this;
    }

    /**
     * Makes the composite a deployable composite.
     *
     * @return the builder
     */
    public CompositeBuilder deployable() {
        checkState();
        composite.setDeployable(true);
        return this;
    }

    /**
     * Sets the runtime modes for the composite.
     *
     * @param modes the runtime modes
     * @return the builder
     */
    public CompositeBuilder mode(List<RuntimeMode> modes) {
        checkState();
        composite.setModes(modes);
        return this;
    }

    /**
     * Sets the runtime environments the composite is activated in.
     *
     * @param environments the runtime environments
     * @return the builder
     */
    public CompositeBuilder environment(List<String> environments) {
        checkState();
        composite.setEnvironments(environments);
        return this;
    }

    /**
     * Builds the composite.
     *
     * @return the built composite
     */
    public Composite build() {
        checkState();
        freeze();
        return composite;
    }

    /**
     * Adds a property to the composite parsed from the XML source.
     *
     * @param name   the property name
     * @param source the XML source
     * @return the builder
     * @throws ModelBuilderException if an error reading the source occurs
     */
    public CompositeBuilder property(String name, URL source) {
        checkState();
        try {
            Document document = DOCUMENT_FACTORY.newDocumentBuilder().parse(source.openStream());
            // all properties have a root <values> element, append the existing root to it. The existing root will be taken as a property <value>.
            Element oldRoot = document.getDocumentElement();
            Element newRoot = document.createElement("values");
            document.removeChild(oldRoot);
            document.appendChild(newRoot);
            newRoot.appendChild(oldRoot);

            Property property = new Property(name);
            property.setDefaultValue(document);
            composite.add(property);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ModelBuilderException(e);
        }
        return this;

    }

    protected CompositeBuilder(QName name) {
        composite = new Composite(name);
    }
}
