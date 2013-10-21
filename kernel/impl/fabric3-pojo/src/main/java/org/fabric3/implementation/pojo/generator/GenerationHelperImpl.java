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
package org.fabric3.implementation.pojo.generator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.pojo.provision.PojoComponentDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.physical.PhysicalPropertyDefinition;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.api.model.type.java.Signature;

/**
 *
 */
public class GenerationHelperImpl implements GenerationHelper {

    public void processInjectionSites(InjectingComponentType componentType, ImplementationManagerDefinition managerDefinition) {

        Map<InjectionSite, Injectable> mappings = componentType.getInjectionSites();

        // add injections for all the active constructor args
        Map<InjectionSite, Injectable> construction = managerDefinition.getConstruction();
        Signature constructor = componentType.getConstructor();
        Set<Injectable> byConstruction = new HashSet<Injectable>(constructor.getParameterTypes().size());
        for (int i = 0; i < constructor.getParameterTypes().size(); i++) {
            InjectionSite site = new ConstructorInjectionSite(constructor, i);
            Injectable attribute = mappings.get(site);
            construction.put(site, attribute);
            byConstruction.add(attribute);
        }

        // add field/method injections
        Map<InjectionSite, Injectable> postConstruction = managerDefinition.getPostConstruction();
        Map<InjectionSite, Injectable> reinjection = managerDefinition.getReinjectables();
        for (Map.Entry<InjectionSite, Injectable> entry : mappings.entrySet()) {
            InjectionSite site = entry.getKey();
            if (site instanceof ConstructorInjectionSite) {
                continue;
            }

            Injectable attribute = entry.getValue();
            if (!byConstruction.contains(attribute)) {
                postConstruction.put(site, attribute);
            }
            reinjection.put(site, attribute);
        }
    }

    public void processPropertyValues(LogicalComponent<?> component, PojoComponentDefinition physical) {
        for (LogicalProperty property : component.getAllProperties().values()) {
            String name = property.getName();
            boolean many = property.isMany();
            if (property.getValue() != null) {
                Document document = property.getValue();
                PhysicalPropertyDefinition definition = new PhysicalPropertyDefinition(name, document, many);
                physical.setPropertyDefinition(definition);
            } else if (property.getInstanceValue() != null) {
                Object value = property.getInstanceValue();
                PhysicalPropertyDefinition definition = new PhysicalPropertyDefinition(name, value, many);
                physical.setPropertyDefinition(definition);
            }
        }
    }
}
