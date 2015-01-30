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
package org.fabric3.api.model.type.component;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * A reference.
 */
public class ReferenceDefinition<P extends ModelObject> extends BindableDefinition<P> {
    private static final long serialVersionUID = 4641581818938572132L;

    private String name;

    private ServiceContract serviceContract;
    private Multiplicity multiplicity;

    private boolean keyed;
    private DataType keyDataType;

    private List<BindingDefinition> callbackBindings = new ArrayList<>();

    private List<Target> targets = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param name            the reference name
     * @param serviceContract the service contract required by this reference
     */
    public ReferenceDefinition(String name, ServiceContract serviceContract) {
        this(name, serviceContract, Multiplicity.ONE_ONE);
    }

    /**
     * Constructor.
     *
     * @param name         the reference name
     * @param multiplicity the reference multiplicity
     */
    public ReferenceDefinition(String name, Multiplicity multiplicity) {
        this(name, null, multiplicity);
    }

    /**
     * Constructor.
     *
     * @param name the reference name
     */
    public ReferenceDefinition(String name) {
        this(name, null, Multiplicity.ONE_ONE);
    }

    /**
     * Constructor.
     *
     * @param name            the reference name
     * @param serviceContract the service contract required by this reference
     * @param multiplicity    the reference multiplicity
     */
    public ReferenceDefinition(String name, ServiceContract serviceContract, Multiplicity multiplicity) {
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
    public DataType getKeyDataType() {
        return keyDataType;
    }

    /**
     * Sets the reference key type.
     *
     * @param keyDataType the reference key type
     */
    public void setKeyDataType(DataType keyDataType) {
        this.keyDataType = keyDataType;
    }


}
