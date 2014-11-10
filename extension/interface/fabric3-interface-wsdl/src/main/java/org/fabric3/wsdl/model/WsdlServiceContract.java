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
 */
package org.fabric3.wsdl.model;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * WSDL Service contract.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class WsdlServiceContract extends ServiceContract {
    private static final long serialVersionUID = 8084985972954894699L;
    private Map<QName, Object> extensionElements = new HashMap<>();
    private PortType portType;
    private Definition definition;

    public WsdlServiceContract(PortType portType, Definition definition) {
        this.portType = portType;
        this.definition = definition;
    }

    public String getQualifiedInterfaceName() {
        return portType.getQName().toString();
    }

    public boolean isRemotable() {
        return true;
    }

    public void setRemotable(boolean remotable) {
        if (!remotable) {
            throw new IllegalArgumentException("WSDL interfaces are always remotable");
        }
    }

    /**
     * Returns the PortType this contract is defined by.
     *
     * @return the PortType
     */
    public PortType getPortType() {
        return portType;
    }

    /**
     * Returns the qualified WSDL name.
     *
     * @return the qualified WSDL name
     */
    public QName getWsdlQName() {
        return definition.getQName();
    }

    /**
     * Returns the containing WSDL.
     *
     * @return the containing WSDL
     */
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Adds an extension element.
     *
     * @param key     the element key
     * @param element the extension element
     */
    public void addExtensionElement(QName key, Object element) {
        extensionElements.put(key, element);
    }

    /**
     * Returns the extension element.
     *
     * @param type the element type
     * @param key  the element key
     * @return the extension element.
     */
    public <T> T getExtensionElement(Class<T> type, QName key) {
        return type.cast(extensionElements.get(key));
    }

    /**
     * Returns all extension elements.
     *
     * @return all extension elements
     */
    public Map<QName, Object> getExtensionElements() {
        return extensionElements;
    }

    /**
     * Performs a shallow instance copy.
     *
     * @return a copy of the current instance.
     */
    public WsdlServiceContract copy() {
        WsdlServiceContract copy = new WsdlServiceContract(portType, definition);
        copy.setCallbackContract(callbackContract);
        copy.setIntents(getIntents());
        copy.setInterfaceName(interfaceName);
        copy.setOperations(operations);
        copy.setPolicySets(getPolicySets());
        for (Map.Entry<QName, Object> entry : extensionElements.entrySet()) {
            copy.addExtensionElement(entry.getKey(), entry.getValue());
        }
        return copy;
    }
}
