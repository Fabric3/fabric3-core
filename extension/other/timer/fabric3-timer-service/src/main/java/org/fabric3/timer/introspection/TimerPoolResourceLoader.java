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
package org.fabric3.timer.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.model.type.resource.timer.TimerPoolResource;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class TimerPoolResourceLoader extends AbstractValidatingTypeLoader<TimerPoolResource> {
    public TimerPoolResourceLoader() {
        addAttributes("name", "size");
    }

    public TimerPoolResource load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute error = new MissingAttribute("Name not specified for timer pool", startLocation);
            context.addError(error);
            return new TimerPoolResource("error");
        }
        String coreSizeAttr = reader.getAttributeValue(null, "size");
        TimerPoolResource resource;
        if (coreSizeAttr == null) {
            resource = new TimerPoolResource(name);
        } else {
            try {
                int coreSize = Integer.parseInt(coreSizeAttr);
                resource = new TimerPoolResource(name, coreSize);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("Invalid core size specified for timer pool", startLocation, e);
                context.addError(error);
                return new TimerPoolResource(name);
            }
        }
        validateAttributes(reader, context, resource);

        LoaderUtil.skipToEndElement(reader);
        return resource;
    }

}