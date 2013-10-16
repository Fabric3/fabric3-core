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
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;

/**
 * Represents the target side of a physical wire.
 */
public abstract class PhysicalTargetDefinition implements Serializable {
    private static final long serialVersionUID = -8430498259706831133L;

    private URI uri;
    private boolean optimizable;
    private boolean callback;
    private URI callbackUri;
    private URI classLoaderId;
    protected List<DataType<?>> physicalDataTypes = new ArrayList<DataType<?>>();

    public PhysicalTargetDefinition() {
        // default to Java
        physicalDataTypes.add(PhysicalDataTypes.JAVA_TYPE);
    }

    public PhysicalTargetDefinition(DataType<?>... types) {
        if (types != null) {
            physicalDataTypes.addAll(Arrays.asList(types));
        }
    }

    /**
     * Returns the URI of the physical component targeted by this wire.
     *
     * @return the URI of the physical component targeted by this wire
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI of the physical component targeted by this wire.
     *
     * @param uri the URI of the physical component targeted by this wire
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the URI for the target callback component for invocations passed through this wire.
     *
     * @return the target callback uri or null if the wire is unidirectional
     */
    public URI getCallbackUri() {
        return callbackUri;
    }

    /**
     * Sets the URI for the target callback component for invocations passed through this wire.
     *
     * @param uri the target callback uri
     */
    public void setCallbackUri(URI uri) {
        this.callbackUri = uri;
    }

    /**
     * Returns true if the wire is a callback wire.
     *
     * @return true if the wire is a callback wire
     */
    public boolean isCallback() {
        return callback;
    }

    /**
     * Sets if the wire is a callback wire.
     *
     * @param callback true if the wire is a callback wire
     */
    public void setCallback(boolean callback) {
        this.callback = callback;
    }

    /**
     * Returns a list of supported physical data types by order of preference.
     *
     * @return a list of supported physical data types by order of preference
     */
    public List<DataType<?>> getPhysicalDataTypes() {
        return physicalDataTypes;
    }

    /**
     * Returns whether the target side of the wire is optimizable.
     *
     * @return true if the target side of the wire is optimizable
     */
    public boolean isOptimizable() {
        return optimizable;
    }

    /**
     * Sets whether the target side of the wire is optimizable.
     *
     * @param optimizable whether the target side of the wire is optimizable
     */
    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }

    /**
     * Returns the id of the classloader associated with the target component.
     *
     * @return the id of the classloader associated with the target component
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * Sets the id of the classloader associated with the target component.
     *
     * @param classLoaderId the id of the classloader associated with the target component
     */
    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

}
