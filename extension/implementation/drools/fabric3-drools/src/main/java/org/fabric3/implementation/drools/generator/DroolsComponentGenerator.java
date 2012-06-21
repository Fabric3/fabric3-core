/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.implementation.drools.generator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import org.fabric3.implementation.drools.model.DroolsImplementation;
import org.fabric3.implementation.drools.model.DroolsProperty;
import org.fabric3.implementation.drools.provision.DroolsComponentDefinition;
import org.fabric3.implementation.drools.provision.DroolsPropertyDefinition;
import org.fabric3.implementation.drools.provision.DroolsSourceDefinition;
import org.fabric3.implementation.drools.provision.DroolsTargetDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.type.java.InjectableType;

public class DroolsComponentGenerator implements ComponentGenerator<LogicalComponent<DroolsImplementation>> {

    public DroolsComponentDefinition generate(LogicalComponent<DroolsImplementation> component) throws GenerationException {
        DroolsImplementation implementation = component.getDefinition().getImplementation();
        ComponentType componentType = implementation.getComponentType();
        List<DroolsPropertyDefinition> properties = generateProperties(component, componentType);
        return new DroolsComponentDefinition(implementation.getPackages(), properties);
    }

    public DroolsSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        ServiceContract serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        String name = reference.getDefinition().getName();
        URI uri = reference.getUri();
        return new DroolsSourceDefinition(uri, name, interfaceName, InjectableType.REFERENCE);
    }

    @SuppressWarnings({"unchecked"})
    public DroolsTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        LogicalComponent<DroolsImplementation> component = (LogicalComponent<DroolsImplementation>) service.getLeafComponent();
        URI uri = URI.create(component.getUri().toString() + "#" + service.getUri().getFragment());
        return new DroolsTargetDefinition(uri);
    }

    public DroolsSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        ServiceContract callbackContract = service.getDefinition().getServiceContract().getCallbackContract();
        String interfaceName = callbackContract.getQualifiedInterfaceName();
        String name = service.getDefinition().getName();
        URI uri = service.getUri();
        return new DroolsSourceDefinition(uri, name, interfaceName, InjectableType.CALLBACK);
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    private List<DroolsPropertyDefinition> generateProperties(LogicalComponent<DroolsImplementation> component, ComponentType componentType)
            throws GenerationException {
        List<DroolsPropertyDefinition> properties = new ArrayList<DroolsPropertyDefinition>();
        for (Property property : componentType.getProperties().values()) {
            DroolsProperty droolsProperty = (DroolsProperty) property;
            String name = droolsProperty.getName();
            String type = droolsProperty.getPropertyType();

            PropertyValue propertyValue = component.getDefinition().getPropertyValues().get(name);
            if (propertyValue == null) {
                // this should not happen as it is checked before generation
                throw new GenerationException("Property not configured: " + name);
            }
            Document value = propertyValue.getValue();
            DroolsPropertyDefinition definition = new DroolsPropertyDefinition(name, type, value);
            properties.add(definition);
        }
        return properties;
    }


}
