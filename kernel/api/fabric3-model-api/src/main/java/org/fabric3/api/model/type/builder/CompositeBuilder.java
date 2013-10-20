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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeReference;
import org.fabric3.api.model.type.component.CompositeService;
import org.fabric3.api.model.type.component.Multiplicity;

/**
 * Builds {@link Composite}s.
 */
public class CompositeBuilder extends AbstractBuilder {
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
    public CompositeBuilder add(ComponentDefinition<?> definition) {
        checkState();
        composite.add(definition);
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
