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
*/
package org.fabric3.xquery.provision;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.w3c.dom.Document;

/**
 *
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class XQueryComponentDefinition extends PhysicalComponentDefinition {
    private static final long serialVersionUID = -2176668190738465467L;

    private String location;
    private String context;
    private Map<String, Document> propertyValues = new HashMap<>();
    private Map<String, List<QName>> serviceFunctions;
    private Map<String, List<QName>> referenceFunctions;
    private Map<String, List<QName>> callbackFunctions;
    //private Map<String, List<QName>> serviceCallbackFunctions;
    //private Map<String, List<QName>> referenceCallbackFunctions;
    private Map<String, QName> properties;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Document> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValue(String name, Document value) {
        propertyValues.put(name, value);
    }

    public Map<String, List<QName>> getServiceFunctions() {
        return serviceFunctions;
    }

    public Map<String, List<QName>> getReferenceFunctions() {
        return referenceFunctions;
    }


    public Map<String, QName> getProperties() {
        return properties;
    }

    public void setServiceFunctions(Map<String, List<QName>> serviceFunctions) {
        this.serviceFunctions = serviceFunctions;
    }

    public void setReferenceFunctions(Map<String, List<QName>> referenceFunctions) {
        this.referenceFunctions = referenceFunctions;
    }


    public void setCallbackFunctions(Map<String, List<QName>> callbackFunctions) {
        this.callbackFunctions = callbackFunctions;
    }

    public Map<String, List<QName>> getCallbackFunctions() {
        return callbackFunctions;
    }

    /*
     public void setReferenceCallbackFunctions(Map<String, List<QName>> callbackFunctions) {
        this.referenceCallbackFunctions = callbackFunctions;
    }

    public Map<String, List<QName>> getReferenceCallbackFunctions() {
        return referenceCallbackFunctions;
    }
    */
    public void setProperties(Map<String, QName> properties) {
        this.properties = properties;
    }
}
