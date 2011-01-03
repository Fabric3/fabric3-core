/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 */
package org.fabric3.wsdl.model;

import java.util.HashMap;
import java.util.Map;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.fabric3.model.type.contract.ServiceContract;

/**
 * WSDL Service contract.
 *
 * @version $Revsion$ $Date$
 */
public class WsdlServiceContract extends ServiceContract {
    private static final long serialVersionUID = 8084985972954894699L;
    private Map<QName, Object> extensionElements = new HashMap<QName, Object>();
    private PortType portType;
    private QName wsdlQName;

    public WsdlServiceContract(PortType portType, QName wsdlQName) {
        this.portType = portType;
        this.wsdlQName = wsdlQName;
    }

    public String getQualifiedInterfaceName() {
        return portType.getQName().toString();
    }

    @Override
    public boolean isRemotable() {
        return true;
    }

    @Override
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
     * Returns the qualied WSDL name.
     *
     * @return the qualied WSDL name
     */
    public QName getWsdlQName() {
        return wsdlQName;
    }

    /**
     * Adds an extension element.
     *
     * @param key     the element key
     * @param element the extension element
     */
    public void addExtensonElement(QName key, Object element) {
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
        WsdlServiceContract copy = new WsdlServiceContract(portType, wsdlQName);
        copy.setCallbackContract(callbackContract);
        copy.setConversational(conversational);
        copy.setIntents(getIntents());
        copy.setInterfaceName(interfaceName);
        copy.setOperations(operations);
        copy.setPolicySets(getPolicySets());
        for (Map.Entry<QName, Object> entry : extensionElements.entrySet()) {
            copy.addExtensonElement(entry.getKey(), entry.getValue());
        }
        return copy;
    }
}
