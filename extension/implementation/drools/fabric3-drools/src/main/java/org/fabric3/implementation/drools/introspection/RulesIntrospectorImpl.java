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
package org.fabric3.implementation.drools.introspection;

import java.util.Map;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;

/**
 * Default introspector implementation.
 * <p/>
 * The following algorithm is applied: if the global type is a primitive, array of primitives or is in the "java" package hierarchy, it is a property;
 * otherwise it is a reference.
 *
 * @version $Rev$ $Date$
 */
public class RulesIntrospectorImpl implements RulesIntrospector {
    private JavaContractProcessor contractProcessor;

    public RulesIntrospectorImpl(@Reference JavaContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    public ComponentType introspect(Map<String, Class<?>> globals, XMLStreamReader reader, IntrospectionContext context) {
        ComponentType componentType = new ComponentType();
        for (Map.Entry<String, Class<?>> entry : globals.entrySet()) {
            String key = entry.getKey();
            Class<?> value = entry.getValue();
            introspectType(key, value, componentType, context);
        }
        return componentType;
    }

    private void introspectType(String name, Class<?> type, ComponentType componentType, IntrospectionContext context) {
        java.lang.Package pkg = type.getPackage();
        if (type.isPrimitive()) {
            createProperty(name, componentType);
        } else if (type.isArray()) {
            Class<?> arrayType = type.getComponentType();
            introspectType(name, arrayType, componentType, context);
        } else if (pkg == null) {
            // no package, default to a reference
            createReference(name, type, componentType, context);
        } else if (pkg.getName().startsWith("java.")) {
            // create a property
            createProperty(name, componentType);
        } else {
            // default to a reference
            createReference(name, type, componentType, context);
        }
    }

    private void createProperty(String name, ComponentType componentType) {
        Property property = new Property(name);
        property.setRequired(true);
        componentType.add(property);
    }

    private void createReference(String name, Class<?> type, ComponentType componentType, IntrospectionContext context) {
        ServiceContract contract = contractProcessor.introspect(type, context);
        ReferenceDefinition definition = new ReferenceDefinition(name, contract, Multiplicity.ONE_ONE);
        componentType.add(definition);
    }


}
