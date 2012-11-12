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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.monitor.introspection;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.fabric3.api.MonitorEvent;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.monitor.model.MonitorImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.model.type.java.JavaClass;

/**
 * Loads information for a monitor implementation
 */
@EagerInit
public class MonitorImplementationLoader implements TypeLoader<MonitorImplementation> {
    private List<DataType<?>> consumerTypes;
    private LoaderHelper helper;

    public MonitorImplementationLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
        consumerTypes = new ArrayList<DataType<?>>();
        consumerTypes.add(new JavaClass<MonitorEvent>(MonitorEvent.class));
    }

    public MonitorImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        validateAttributes(reader, introspectionContext);
        Element configuration = null;
        while (true) {
            int event = reader.next();
            if (XMLStreamConstants.END_ELEMENT == event && "implementation.monitor".equals(reader.getName().getLocalPart())) {
                break;
            } else if (XMLStreamConstants.START_ELEMENT == event && "configuration".equals(reader.getName().getLocalPart())) {
                if (reader.getName().getLocalPart().contains("configuration")) {
                    // configuration is optional
                    Document document = helper.transform(reader);
                    if (document != null) {
                        NodeList list = document.getElementsByTagName("configuration");
                        if (list.getLength() == 1) {
                            configuration = (Element) list.item(0);
                        }
                    }
                }
            }
        }
//        LoaderUtil.skipToEndElement(reader);
        ComponentType type = new ComponentType();
        type.add(new ConsumerDefinition("monitor", consumerTypes));
        return new MonitorImplementation(type, configuration);
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            // no attributes are legal
            String name = reader.getAttributeLocalName(i);
            context.addError(new UnrecognizedAttribute(name, location));
        }
    }

}