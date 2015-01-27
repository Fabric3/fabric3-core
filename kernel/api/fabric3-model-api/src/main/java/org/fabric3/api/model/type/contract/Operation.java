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
package org.fabric3.api.model.type.contract;

import java.util.Collections;
import java.util.List;

import org.fabric3.api.model.type.AbstractPolicyAware;

/**
 * An operation on a service contract.
 */
public class Operation extends AbstractPolicyAware<ServiceContract> {
    private static final long serialVersionUID = 5279880534105654066L;
    private String name;
    private String wsdlName;
    private boolean remotable;
    private DataType outputType;
    private List<DataType> inputTypes;
    private List<DataType> faultTypes;

    /**
     * Constructor.
     *
     * @param name       the name of the operation
     * @param inputTypes the data types of parameters passed to the operation
     * @param outputType the data type returned by the operation
     * @param faultTypes the data type of faults raised by the operation
     */
    public Operation(String name, List<DataType> inputTypes, DataType outputType, List<DataType> faultTypes) {
        this.name = name;
        List<DataType> types = Collections.emptyList();
        this.inputTypes = inputTypes;
        this.outputType = outputType;
        this.faultTypes = (faultTypes == null) ? types : faultTypes;
        wsdlName = name;
    }

    /**
     * Returns the name of the operation.
     *
     * @return the name of the operation
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the WSDL name.
     *
     * @param wsdlName the WSDL name
     */
    public void setWsdlName(String wsdlName) {
        this.wsdlName = wsdlName;
    }

    /**
     * Returns the data types of the parameters passed to the operation.
     * <p/>
     * The inputType's logical type is a list of DataTypes which describes the parameter types
     *
     * @return the data types of the parameters passed to the operation
     */
    public List<DataType> getInputTypes() {
        return inputTypes;
    }

    /**
     * Returns the data type returned by the operation.
     *
     * @return the data type returned by the operation
     */
    public DataType getOutputType() {
        return outputType;
    }

    /**
     * Returns the data types of the faults raised by the operation.
     *
     * @return the data types of the faults raised by the operation
     */
    public List<DataType> getFaultTypes() {
        if (faultTypes == null) {
            return Collections.emptyList();
        }
        return faultTypes;
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

    public String toString() {
        return "Operation [" + name + "]";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Operation operation = (Operation) o;

        if (name != null ? !name.equals(operation.name) : operation.name != null) {
            return false;
        }

        if (faultTypes == null && operation.faultTypes != null) {
            return false;
        } else if (faultTypes != null
                && operation.faultTypes != null
                && faultTypes.size() != 0
                && operation.faultTypes.size() != 0) {
            if (faultTypes.size() < operation.faultTypes.size()) {
                return false;
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < operation.faultTypes.size(); i++) {
                    if (!faultTypes.get(i).equals(operation.faultTypes.get(i))) {
                        return false;
                    }
                }
            }
        }

        //noinspection SimplifiableIfStatement
        if (inputTypes != null ? !inputTypes.equals(operation.inputTypes) : operation.inputTypes != null) {
            return false;
        }
        return !(outputType != null ? !outputType.equals(operation.outputType) : operation.outputType != null);
    }

    public int hashCode() {
        int result;
        result = name != null ? name.hashCode() : 0;

        result = 29 * result + (outputType != null ? outputType.hashCode() : 0);
        result = 29 * result + (inputTypes != null ? inputTypes.hashCode() : 0);
        result = 29 * result + (faultTypes != null ? faultTypes.hashCode() : 0);
        return result;
    }

}
