/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a service operation and its invocation chain on a runtime. Since the source side of a wire (reference or forward service making a
 * callback) may have a service contract that is different from the target side (forward service or client being called back), in-, out- and fault
 * parameter types are stored for both sides. When attaching a wire to its source or target, the appropriate parameter types must be used.
 *
 * @version $Revision$ $Date$
 */
public class PhysicalOperationDefinition implements Serializable {
    private static final long serialVersionUID = -4270990709748460450L;

    private String name;

    private List<String> parameterTypes = new ArrayList<String>();
    private List<String> faultTypes = new ArrayList<String>();
    private String returnType;

    private List<String> targetParameterTypes = new ArrayList<String>();
    private List<String> targetFaultTypes = new ArrayList<String>();
    private String targetReturnType;

    private boolean callback;
    private boolean oneWay;
    private boolean endsConversation;
    private boolean remotable;
    private boolean allowsPassByReference = true;

    // Interceptors defined against the operation
    private Set<PhysicalInterceptorDefinition> interceptors = new HashSet<PhysicalInterceptorDefinition>();

    /**
     * Gets the name of the operation.
     *
     * @return Operation name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the operation.
     *
     * @param name Operation name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the fully qualified name of source parameter types for this operation. Parameter types are returned in order.
     *
     * @return the source parameter types.
     */
    public List<String> getSourceParameterTypes() {
        return parameterTypes;
    }

    /**
     * Add the fully qualified name of the source parameter type to the operation.
     *
     * @param type the source parameter type to be added.
     */
    public void addSourceParameterType(String type) {
        parameterTypes.add(type);
    }

    /**
     * Returns the fuly qualified source return type for this operation.
     *
     * @return the source return type for this operation.
     */
    public String getSourceReturnType() {
        return returnType;
    }

    /**
     * Sets the fully qualified source return type for this operation.
     *
     * @param type the source return type for this operation.
     */
    public void setSourceReturnType(String type) {
        this.returnType = type;
    }

    /**
     * Returns the fully qualified name of the source fault types.
     *
     * @return the source fault types
     */
    public List<String> getSourceFaultTypes() {
        return faultTypes;
    }

    /**
     * Adds the fully qualified name of a source fault type for this operation.
     *
     * @param type the source fault type
     */
    public void addSourceFaultType(String type) {
        faultTypes.add(type);
    }

    /**
     * Returns the fully qualified name of target parameter types for this operation. Parameter types are returned in order.
     *
     * @return the target parameter types.
     */
    public List<String> getTargetParameterTypes() {
        return targetParameterTypes;
    }

    /**
     * Add the fully qualified name of the target parameter type to the operation.
     *
     * @param type the target parameter type to be added.
     */
    public void addTargetParameterType(String type) {
        this.targetParameterTypes.add(type);
    }

    /**
     * Returns the fully qualified name of the target fault types.
     *
     * @return the target fault types
     */
    public List<String> getTargetFaultTypes() {
        return targetFaultTypes;
    }

    /**
     * Adds the fully qualified name of a target fault type for this operation.
     *
     * @param type the target fault type
     */
    public void addTargetFaultType(String type) {
        targetFaultTypes.add(type);
    }

    /**
     * Returns the fuly qualified target return type for this operation.
     *
     * @return the target return type for this operation.
     */
    public String getTargetReturnType() {
        return targetReturnType;
    }

    /**
     * Sets the fully qualified target return type for this operation.
     *
     * @param type the target return type for this operation.
     */
    public void setTargetReturnType(String type) {
        this.targetReturnType = type;
    }

    /**
     * Returns the interceptor definitions available for this operation.
     *
     * @return Inteceptor definitions for this operation.
     */
    public Set<PhysicalInterceptorDefinition> getInterceptors() {
        return interceptors;
    }

    /**
     * Sets the interceptor definitions available for this operations.
     *
     * @param interceptors the interceptor definitions available for this operations
     */
    public void setInterceptors(Set<PhysicalInterceptorDefinition> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * Adds an interceptor definition to the operation.
     *
     * @param interceptor Interceptor definition to be added.
     */
    public void addInterceptor(PhysicalInterceptorDefinition interceptor) {
        interceptors.add(interceptor);
    }

    /**
     * Checks whether the operation is a callback.
     *
     * @return True if this is a callback.
     */
    public boolean isCallback() {
        return callback;
    }

    /**
     * Sets whether this is a callback operation or not.
     *
     * @param callback True if this is a callback.
     */
    public void setCallback(boolean callback) {
        this.callback = callback;
    }

    /**
     * Returns true if the operation ends a conversation.
     *
     * @return true if the operation ends a conversation
     */
    public boolean isEndsConversation() {
        return endsConversation;
    }

    /**
     * Sets if the operation ends a conversation.
     *
     * @param endsConversation true if the operation ends a conversation
     */
    public void setEndsConversation(boolean endsConversation) {
        this.endsConversation = endsConversation;
    }

    /**
     * Returns true if the operation is non-blocking.
     *
     * @return true if the operation is non-blocking
     */
    public boolean isOneWay() {
        return oneWay;
    }

    /**
     * Sets if the operation is non-blocking.
     *
     * @param oneWay true if the operation is non-blocking
     */
    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    /**
     * Returns true if the operation is part of a remotable interface.
     *
     * @return true if the operation is part of a remotable interface
     */
    public boolean isRemotable() {
        return remotable;
    }

    /**
     * Sets if the operation is part of a remotable interface.
     *
     * @param remotable true if the operation is part of a remotable interface
     */
    public void setRemotable(boolean remotable) {
        this.remotable = remotable;
    }

    /**
     * Return true if the operation allows pass-by-reference
     *
     * @return true if the operation allows pass-by-reference
     */
    public boolean isAllowsPassByReference() {
        return allowsPassByReference;
    }

    /**
     * Sets if the operation allows pass-by-reference
     *
     * @param allowsPassByReference true if the operation allows pass-by-reference
     */
    public void setAllowsPassByReference(boolean allowsPassByReference) {
        this.allowsPassByReference = allowsPassByReference;
    }
}
