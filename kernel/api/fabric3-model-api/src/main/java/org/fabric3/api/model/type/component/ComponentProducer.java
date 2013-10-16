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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.api.model.type.component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A producer configured on a component.
 */
public class ComponentProducer extends AbstractProducer<ComponentDefinition> {
    private static final long serialVersionUID = -4230400252060306972L;

    private ComponentDefinition<?> parent;
    private List<URI> targets;

    /**
     * Constructor.
     *
     * @param name    the name of the producer being configured
     * @param targets the channel targets
     */
    public ComponentProducer(String name, List<URI> targets) {
        super(name);
        this.targets = targets;
    }

    /**
     * Constructor.
     *
     * @param name the name of the producer being configured
     */
    public ComponentProducer(String name) {
        super(name);
        this.targets = new ArrayList<URI>();
    }

    /**
     * Returns the parent component of this producer.
     *
     * @return the parent component
     */
    public ComponentDefinition<?> getComponent() {
        return parent;
    }

    /**
     * Sets the parent component of this producer.
     *
     * @param parent the parent component
     */
    public void setParent(ComponentDefinition<?> parent) {
        this.parent = parent;
    }

    /**
     * Returns the URIs of channels this producer sends messages to.
     *
     * @return the URIs of channels this producer sends messages to
     */
    public List<URI> getTargets() {
        return targets;
    }

    /**
     * Sets the URIs of channels this producer sends messages to.
     *
     * @param targets the URIs of channels this producer sends messages to
     */
    public void setTargets(List<URI> targets) {
        this.targets = targets;
    }

    /**
     * Adds the URI of a channel this producer sends messages to.
     *
     * @param target the channel URI
     */
    public void addTarget(URI target) {
        targets.add(target);
    }

}