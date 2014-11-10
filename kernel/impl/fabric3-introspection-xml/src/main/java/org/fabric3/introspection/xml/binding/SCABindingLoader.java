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

package org.fabric3.introspection.xml.binding;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.Target;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidTargetException;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.model.type.binding.SCABinding;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Processes the <code>binding.sca</code> element.
 */
public class SCABindingLoader extends AbstractExtensibleTypeLoader<SCABinding> {
    private static final QName BINDING = new QName(Constants.SCA_NS, "binding.sca");
    private LoaderRegistry registry;
    private LoaderHelper helper;

    public SCABindingLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper helper) {
        super(registry);
        this.registry = registry;
        this.helper = helper;
        addAttributes("uri", "requires", "policySets", "name");
    }

    public QName getXMLType() {
        return BINDING;
    }

    public SCABinding load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        Target target = null;
        String uriAttr = reader.getAttributeValue(null, "uri");

        if (uriAttr != null) {
            try {
                target = helper.parseTarget(uriAttr, reader);
            } catch (InvalidTargetException e) {
                InvalidValue error = new InvalidValue("Invalid URI specified on binding.sca", startLocation);
                context.addError(error);
            }
        }
        String name = reader.getAttributeValue(null, "name");
        SCABinding binding = new SCABinding(name, target);
        helper.loadPolicySetsAndIntents(binding, reader, context);

        validateAttributes(reader, context, binding);

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                registry.load(reader, ModelObject.class, context);
                break;
            case END_ELEMENT:
                if ("binding.sca".equals(reader.getName().getLocalPart())) {
                    return binding;
                }
            }
        }
    }

}
