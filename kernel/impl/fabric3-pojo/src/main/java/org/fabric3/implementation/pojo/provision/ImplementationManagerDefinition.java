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
package org.fabric3.implementation.pojo.provision;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.Signature;

/**
 * Base class for implementation manager factory definitions.
 */
public class ImplementationManagerDefinition implements Serializable {
    private static final long serialVersionUID = 3516497485593609161L;

    private String implementationClass;
    private Signature constructor;
    private Signature initMethod;
    private Signature destroyMethod;
    private boolean reinjectable;
    private Map<InjectionSite, Injectable> construction = new HashMap<InjectionSite, Injectable>();
    private Map<InjectionSite, Injectable> postConstruction = new HashMap<InjectionSite, Injectable>();
    private Map<InjectionSite, Injectable> reinjection = new HashMap<InjectionSite, Injectable>();
    private URI componentUri;

    /**
     * Returns the component URI for this implementation definition.
     *
     * @return the component URI for this implementation definition
     */
    public URI getComponentUri() {
        return componentUri;
    }

    /**
     * Sets the component URI for this implementation definition.
     *
     * @param componentUri the component URI for this implementation definition
     */
    public void setComponentUri(URI componentUri) {
        this.componentUri = componentUri;
    }

    /**
     * Returns the signature of the constructor that should be used.
     *
     * @return the signature of the constructor that should be used
     */
    public Signature getConstructor() {
        return constructor;
    }

    /**
     * Sets the signature of the constructor that should be used.
     *
     * @param constructor the signature of the constructor that should be used
     */
    public void setConstructor(Signature constructor) {
        this.constructor = constructor;
    }

    /**
     * Gets the init method.
     *
     * @return the signature for the init method
     */
    public Signature getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the init method.
     *
     * @param initMethod the signature of the init method
     */
    public void setInitMethod(Signature initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Gets the destroy method.
     *
     * @return the signature of the destroy method
     */
    public Signature getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the destroy method.
     *
     * @param destroyMethod the signature of the destroy method
     */
    public void setDestroyMethod(Signature destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    /**
     * Gets the implementation class.
     *
     * @return Implementation class.
     */
    public String getImplementationClass() {
        return implementationClass;
    }

    /**
     * Sets the implementation class.
     *
     * @param implementationClass Implementation class.
     */
    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    /**
     * Returns the map of injections to be performed during construction.
     *
     * @return the map of injections to be performed during construction
     */
    public Map<InjectionSite, Injectable> getConstruction() {
        return construction;
    }

    /**
     * Returns the map of injections to be performed after construction.
     *
     * @return the map of injections to be performed after construction
     */
    public Map<InjectionSite, Injectable> getPostConstruction() {
        return postConstruction;
    }

    /**
     * Returns the map of injections to be performed during reinjection.
     *
     * @return the map of injections to be performed during reinjection
     */
    public Map<InjectionSite, Injectable> getReinjectables() {
        return reinjection;
    }

    /**
     * Returns true if the implementation is reinjectable, e.g. it is composite-scoped.
     *
     * @return true if the implementation is reinjectable, e.g. it is composite-scoped
     */
    public boolean isReinjectable() {
        return reinjectable;
    }

    /**
     * Sets if the implementation is reinjectable.
     *
     * @param reinjectable true if the implementation is reinjectable
     */
    public void setReinjectable(boolean reinjectable) {
        this.reinjectable = reinjectable;
    }

}
