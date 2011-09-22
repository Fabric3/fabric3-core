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
package org.fabric3.implementation.junit.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.implementation.junit.common.ContextConfiguration;
import org.fabric3.implementation.junit.model.JUnitBindingDefinition;
import org.fabric3.implementation.junit.model.JUnitImplementation;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class JUnitImplementationLoader implements TypeLoader<JUnitImplementation> {

    private final JUnitImplementationProcessor implementationProcessor;

    public JUnitImplementationLoader(@Reference JUnitImplementationProcessor implementationProcessor) {
        this.implementationProcessor = implementationProcessor;
    }

    public JUnitImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String className = reader.getAttributeValue(null, "class");

        JUnitImplementation definition = new JUnitImplementation(className);
        implementationProcessor.introspect(definition, context);
        // Add a binding only on the JUnit service (which is the impl class) so wires are generated to the test operations.
        // These wires will be used by the testing runtime to dispatch to the JUnit components.
        ContextConfiguration configuration = loadConfiguration(reader, context);
        for (ServiceDefinition serviceDefinition : definition.getComponentType().getServices().values()) {
            if (serviceDefinition.getServiceContract().getQualifiedInterfaceName().equals(definition.getImplementationClass())) {
                JUnitBindingDefinition bindingDefinition = new JUnitBindingDefinition(configuration);
                serviceDefinition.addBinding(bindingDefinition);
                break;
            }
        }
        return definition;
    }

    private ContextConfiguration loadConfiguration(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        ContextConfiguration configuration = null;
        String name;
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("configuration".equals(name)) {
                    configuration = new ContextConfiguration();
                }
                if ("username".equals(name)) {
                    if (configuration == null) {
                        InvalidContextConfiguraton error =
                                new InvalidContextConfiguraton("Username element must be contained within a configuration element", reader);
                        context.addError(error);
                    } else {
                        configuration.setUsername(reader.getElementText());
                    }
                } else if ("password".equals(name)) {
                    if (configuration == null) {
                        InvalidContextConfiguraton error =
                                new InvalidContextConfiguraton("Password element must be contained within a configuration element", reader);
                        context.addError(error);
                    } else {
                        configuration.setPassword(reader.getElementText());
                    }
                }
                break;
            case END_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("junit".equals(name)) {
                    return configuration;
                }
                break;
            }
        }
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"class".equals(name) && !"requires".equals(name) && !"policySets".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }


}
