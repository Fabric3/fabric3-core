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

package org.fabric3.introspection.xml.binding;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.Target;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidTargetException;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;
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
        validateAttributes(reader, context);
        Target target = null;
        String uriAttr = reader.getAttributeValue(null, "uri");

        if (uriAttr != null) {
            try {
                target = helper.parseTarget(uriAttr, reader);
            } catch (InvalidTargetException e) {
                InvalidValue error = new InvalidValue("Invalid URI specified on binding.sca", reader);
                context.addError(error);
            }
        }
        String name = reader.getAttributeValue(null, "name");
        SCABinding binding = new SCABinding(name, target);
        helper.loadPolicySetsAndIntents(binding, reader, context);
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                try {
                    registry.load(reader, ModelObject.class, context);
                } catch (UnrecognizedElementException e) {
                    UnrecognizedElement error = new UnrecognizedElement(reader);
                    context.addError(error);
                    continue;
                }
                break;
            case END_ELEMENT:
                if ("binding.sca".equals(reader.getName().getLocalPart())) {
                    return binding;
                }
            }
        }
    }

}
