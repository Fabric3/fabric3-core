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
 */
public class PhysicalOperationDefinition implements Serializable, Comparable {
    private static final long serialVersionUID = -4270990709748460450L;

    private String name;

    private List<String> parameterTypes = new ArrayList<>();
    private List<String> faultTypes = new ArrayList<>();
    private String returnType;

    private List<String> targetParameterTypes = new ArrayList<>();
    private List<String> targetFaultTypes = new ArrayList<>();
    private String targetReturnType;

    private boolean callback;
    private boolean oneWay;
    private boolean remotable;
    private boolean allowsPassByReference = true;
    private String compareString;

    // Interceptors defined against the operation
    private Set<PhysicalInterceptorDefinition> interceptors = new HashSet<>();

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
     * Returns the fully qualified source return type for this operation.
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
     * Returns the fully qualified target return type for this operation.
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
     * @return Interceptor definitions for this operation.
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

    /**
     * Implementation that relies on comparing a string representation of the operation name and input parameters.
     *
     * @param o the operation to compare against
     * @return the compare value
     */
    public int compareTo(Object o) {
        if (!(o instanceof PhysicalOperationDefinition)) {
            throw new ClassCastException("Specified object must be of type " + getClass().getName());
        }
        PhysicalOperationDefinition other = (PhysicalOperationDefinition) o;
        return getCompareString().compareTo(other.getCompareString());
    }

    protected String getCompareString() {
        if (compareString == null) {
            StringBuilder builder = new StringBuilder(name);
            for (String type : parameterTypes) {
                builder.append(type);
            }
            for (String type : targetParameterTypes) {
                builder.append(type);
            }
            compareString = builder.toString();
        }
        return compareString;
    }
}
