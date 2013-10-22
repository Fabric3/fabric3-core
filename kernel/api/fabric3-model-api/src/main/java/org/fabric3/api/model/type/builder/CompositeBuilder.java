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
package org.fabric3.api.model.type.builder;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeReference;
import org.fabric3.api.model.type.component.CompositeService;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Property;
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
     * Promotes a service provided by a contained component.
     *
     * @param name     the promoted service name
     * @param promoted the name of the service to promote. The name is specified as the component name/service name. If the component only provides one service
     *                 (e.g. it implements one interface), the service name part may be omitted.
     * @return the builder
     */
    public CompositeBuilder promoteService(String name, String promoted) {
        checkState();
        CompositeService compositeService = new CompositeService(name, URI.create(promoted));
        composite.add(compositeService);
        return this;
    }

    /**
     * Promotes a reference on a contained component.
     *
     * @param name     the promoted reference name
     * @param promoted the name of the reference to promote. The name is specified as the component name/reference name. If the component only provides one
     *                 reference, the reference name part may be omitted.
     * @return the builder
     */
    public CompositeBuilder promoteReference(String name, String promoted) {
        checkState();
        CompositeReference compositeService = new CompositeReference(name, Collections.singletonList(URI.create(promoted)), Multiplicity.ONE_ONE);
        composite.add(compositeService);
        return this;
    }

    /**
     * Promotes multiple references provided by more than one contained component using a single promoted reference.
     *
     * @param name     the promoted reference name
     * @param promoted the name of the references to promote. The name is specified as the component name/reference name. If the component only provides one
     *                 reference, the reference name part may be omitted.
     * @return the builder
     */
    public CompositeBuilder promoteReferences(String name, Multiplicity multiplicity, List<String> promoted) {
        checkState();
        List<URI> uris = new ArrayList<URI>();
        for (String value : promoted) {
            uris.add(URI.create(value));
        }
        CompositeReference compositeService = new CompositeReference(name, uris, multiplicity);
        composite.add(compositeService);
        return this;
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
        } catch (IOException e) {
            throw new ModelBuilderException(e);
        } catch (SAXException e) {
            throw new ModelBuilderException(e);
        } catch (ParserConfigurationException e) {
            throw new ModelBuilderException(e);
        }
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

    protected CompositeBuilder(QName name) {
        composite = new Composite(name);
    }
}
