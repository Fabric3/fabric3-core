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
package org.fabric3.introspection.xml.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.oasisopen.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.xml.composite.AbstractExtensibleTypeLoader;
import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Target;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidTargetException;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;

/**
 * Loads a reference from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentReferenceLoader extends AbstractExtensibleTypeLoader<ComponentReference> {
    private static final QName REFERENCE = new QName(SCA_NS, "reference");
    private static final QName CALLBACK = new QName(SCA_NS, "callback");
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("autowire", "autowire");
        ATTRIBUTES.put("target", "target");
        ATTRIBUTES.put("multiplicity", "multiplicity");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
    }

    private LoaderHelper loaderHelper;

    public ComponentReferenceLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper loaderHelper) {
        super(registry);
        this.loaderHelper = loaderHelper;
    }

    public QName getXMLType() {
        return REFERENCE;
    }

    public ComponentReference load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingReferenceName failure = new MissingReferenceName(reader);
            context.addError(failure);
            return null;
        }

        boolean autowire = Boolean.parseBoolean(reader.getAttributeValue(null, "autowire"));

        String value = reader.getAttributeValue(null, "multiplicity");
        Multiplicity multiplicity = null;
        try {
            if (value != null) {
                multiplicity = Multiplicity.fromString(value);
            }
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid multiplicity value: " + value, reader);
            context.addError(failure);
        }
        ComponentReference reference = new ComponentReference(name, multiplicity);
        reference.setAutowire(autowire);

        String targetAttribute = reader.getAttributeValue(null, "target");
        List<Target> targets = new ArrayList<Target>();
        try {
            if (targetAttribute != null) {
                StringTokenizer tokenizer = new StringTokenizer(targetAttribute);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    Target target = loaderHelper.parseTarget(token, reader);
                    targets.add(target);
                }
            }
        } catch (InvalidTargetException e) {
            InvalidValue failure = new InvalidValue("Invalid target format", reader, e);
            context.addError(failure);
        }
        reference.getTargets().addAll(targets);

        loaderHelper.loadPolicySetsAndIntents(reference, reader, context);

        boolean callback = false;
        boolean bindingError = false;  // used to avoid reporting multiple binding errors
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                callback = CALLBACK.equals(reader.getName());
                if (callback) {
                    reader.nextTag();
                }
                QName elementName = reader.getName();
                ModelObject type;
                try {
                    type = registry.load(reader, ModelObject.class, context);
                    // TODO when the loader registry is replaced this try..catch must be replaced with a check for a loader and an
                    // UnrecognizedElement added to the context if none is found
                } catch (UnrecognizedElementException e) {
                    UnrecognizedElement failure = new UnrecognizedElement(reader);
                    context.addError(failure);
                    continue;
                }
                if (type instanceof ServiceContract) {
                    reference.setServiceContract((ServiceContract) type);
                } else if (type instanceof BindingDefinition) {
                    BindingDefinition binding = (BindingDefinition) type;
                    if (!reference.getTargets().isEmpty()) {
                        if (!bindingError) {
                            // bindings cannot be configured on references if the @target attribute is used
                            InvalidBinding error =
                                    new InvalidBinding("Bindings cannot be configured when the target attribute on a reference is used: "
                                            + name, reader);
                            context.addError(error);
                            bindingError = true;
                        }
                        continue;
                    }
                    if (callback) {
                        if (binding.getName() == null) {
                            // set the default binding name
                            BindingHelper.configureName(binding, name, reference.getCallbackBindings(), reader, context);
                        }
                        reference.addCallbackBinding(binding);
                    } else {
                        if (binding.getName() == null) {
                            // set the default binding name
                            BindingHelper.configureName(binding, name, reference.getBindings(), reader, context);
                        }
                        reference.addBinding((BindingDefinition) type);
                    }
                } else if (type == null) {
                    // no type, continue processing
                    continue;
                } else {
                    context.addError(new UnrecognizedElement(reader));
                    continue;
                }
                if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                    throw new AssertionError("Loader must position the cursor to the end element");
                }
                break;
            case END_ELEMENT:
                if (callback) {
                    callback = false;
                    break;
                }
                return reference;
            }
        }
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }
}
