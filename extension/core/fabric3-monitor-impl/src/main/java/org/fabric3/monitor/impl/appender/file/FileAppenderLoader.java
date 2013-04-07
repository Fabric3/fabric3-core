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
package org.fabric3.monitor.impl.appender.file;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.Namespaces;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a {@link FileAppenderDefinition} from an appender configuration.
 */
@EagerInit
public class FileAppenderLoader extends AbstractValidatingTypeLoader<FileAppenderDefinition> {
    private static final QName SCA_TYPE = new QName(Constants.SCA_NS, "appender.file");
    private static final QName F3_TYPE = new QName(Namespaces.F3, "appender.file");

    private static final long SIZE_100MB = 104857600;

    private LoaderRegistry registry;
    private LoaderMonitor monitor;

    public FileAppenderLoader(@Reference LoaderRegistry registry, @Monitor LoaderMonitor monitor) {
        this.registry = registry;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        // register under both namespaces
        registry.registerLoader(F3_TYPE, this);
        registry.registerLoader(SCA_TYPE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(F3_TYPE);
        registry.unregisterLoader(SCA_TYPE);
    }

    public FileAppenderDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        addAttributes("file", "roll.type", "roll.size");
        validateAttributes(reader, context);
        String fileName = reader.getAttributeValue(null, "file");
        Location location = reader.getLocation();
        if (fileName == null) {
            FileAppenderDefinition definition = new FileAppenderDefinition("");
            MissingAttribute error = new MissingAttribute("A file must be defined for the appender", location, definition);
            context.addError(error);
            return definition;
        }

        String rollType = reader.getAttributeValue(null, "roll.type");
        long rollSize = SIZE_100MB;
        if (rollType == null || FileAppenderConstants.ROLL_STRATEGY_NONE.equals(rollType)) {
            return new FileAppenderDefinition(fileName);
        } else if (FileAppenderConstants.ROLL_STRATEGY_SIZE.equals(rollType)) {
            String sizeStr = reader.getAttributeValue(null, "roll.size");
            if (sizeStr != null) {
                try {
                    rollSize = Long.parseLong(sizeStr);
                } catch (NumberFormatException e) {
                    monitor.invalidRollSize(fileName, sizeStr);
                }
            }
            return new FileAppenderDefinition(fileName, rollType, rollSize);
        } else {
            monitor.invalidRollType(fileName, rollType);
            return new FileAppenderDefinition(fileName);
        }
    }

}