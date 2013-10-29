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

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * An abstract reference type.
 */
public abstract class AbstractReference<P extends ModelObject> extends BindableDefinition<P> {
    private static final long serialVersionUID = -3579257861036837317L;

    private String name;

    private ServiceContract serviceContract;
    private Multiplicity multiplicity;
    private Autowire autowire = Autowire.INHERITED;

    private boolean keyed;
    private DataType<?> keyDataType;

    private List<BindingDefinition> callbackBindings = new ArrayList<BindingDefinition>();

    protected List<Target> targets = new ArrayList<Target>();

    /**
     * Constructor.
     *
     * @param name            the reference name
     * @param serviceContract the service contract required by this reference
     * @param multiplicity    the reference multiplicity
     */
    public AbstractReference(String name, ServiceContract serviceContract, Multiplicity multiplicity) {
        this.name = name;
        this.serviceContract = serviceContract;
        if (serviceContract != null) {
            serviceContract.setParent(this);
        }
        this.multiplicity = multiplicity;
    }

    /**
     * Returns the reference name.
     *
     * @return the reference name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the service contract required by this reference.
     *
     * @return the service contract required by this reference
     */
    public ServiceContract getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the service contract required by this reference.
     *
     * @param serviceContract the service contract required by this reference
     */
    public void setServiceContract(ServiceContract serviceContract) {
        this.serviceContract = serviceContract;
    }

    /**
     * Returns the reference multiplicity.
     *
     * @return the reference multiplicity
     */
    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    /**
     * Sets the reference multiplicity.
     *
     * @param multiplicity the reference multiplicity
     */
    public void setMultiplicity(Multiplicity multiplicity) {
        this.multiplicity = multiplicity;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void addTargets(List<Target> targets) {
        this.targets.addAll(targets);
    }

    public void addTarget(Target target) {
        targets.add(target);
    }

    /**
     * Returns true if the reference is required
     *
     * @return true if the reference is required
     */
    public boolean isRequired() {
        return multiplicity == Multiplicity.ONE_ONE || multiplicity == Multiplicity.ONE_N;
    }

    /**
     * Returns the autowire setting for the reference.
     *
     * @return true if autowire is enabled for the reference.
     */
    public Autowire getAutowire() {
        return autowire;
    }

    /**
     * Sets autowire for the reference.
     *
     * @param autowire true if autowire is enabled.
     */
    public void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }

    /**
     * Returns the callback bindings configured on the reference
     *
     * @return the callback bindings configured on the reference.
     */
    public List<BindingDefinition> getCallbackBindings() {
        return callbackBindings;
    }

    /**
     * Adds a configured callback binding.
     *
     * @param binding callback binding to be added
     */
    public void addCallbackBinding(BindingDefinition binding) {
        this.callbackBindings.add(binding);
    }

    /**
     * Returns true if the reference is a keyed reference, i.e. is a map-style multiplicity.
     *
     * @return true if the reference is a keyed reference
     */
    public boolean isKeyed() {
        return keyed;
    }

    /**
     * Sets if if the reference is a keyed reference.
     *
     * @param keyed true if the reference is a keyed reference
     */
    public void setKeyed(boolean keyed) {
        this.keyed = keyed;
    }

    /**
     * Returns the reference key type.
     *
     * @return the reference key type.
     */
    public DataType<?> getKeyDataType() {
        return keyDataType;
    }

    /**
     * Sets the reference key type.
     *
     * @param keyDataType the reference key type
     */
    public void setKeyDataType(DataType<?> keyDataType) {
        this.keyDataType = keyDataType;
    }

}
