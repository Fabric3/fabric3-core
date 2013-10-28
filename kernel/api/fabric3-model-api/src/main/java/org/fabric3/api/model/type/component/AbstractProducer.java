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

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * An abstract producer type.
 */
public abstract class AbstractProducer<P extends ModelObject> extends BindableDefinition<P> {
    private static final long serialVersionUID = -5994359066654367488L;

    private String name;
    private ServiceContract serviceContract;

    protected List<URI> targets = new ArrayList<URI>();

    /**
     * Constructor.
     *
     * @param name            the producer name
     * @param serviceContract the service contract required by this producer
     */
    public AbstractProducer(String name, ServiceContract serviceContract) {
        this.name = name;
        this.serviceContract = serviceContract;
        if (serviceContract != null) {
            serviceContract.setParent(this);
        }
    }

    /**
     * Constructor.
     *
     * @param name the producer name
     */
    public AbstractProducer(String name) {
        this(name, null);
    }

    /**
     * Returns the producer name.
     *
     * @return the producer name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the service contract required by this producer.
     *
     * @return the service contract required by this producer
     */
    public ServiceContract getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the service contract required by this producer.
     *
     * @param serviceContract the service contract required by this producer
     */
    public void setServiceContract(ServiceContract serviceContract) {
        this.serviceContract = serviceContract;
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