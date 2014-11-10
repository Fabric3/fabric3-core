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
package org.fabric3.monitor.appender.file;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.monitor.Monitor;
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
    private static final QName F3_TYPE = new QName(org.fabric3.api.Namespaces.F3, "appender.file");
    private static final String FABRIC3_LOG = "fabric3.log";
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
        addAttributes("file", "roll.type", "roll.size", "max.backups");
        validateAttributes(reader, context);
        String fileName = reader.getAttributeValue(null, "file");
        Location location = reader.getLocation();
        if (fileName == null) {
            FileAppenderDefinition definition = new FileAppenderDefinition("");
            MissingAttribute error;
            if (location == null) {
                // system config
                fileName = FABRIC3_LOG;

            } else {
                error = new MissingAttribute("A file must be defined for the appender", location, definition);
                context.addError(error);
                return definition;
            }
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
            String maxBackupsStr = reader.getAttributeValue(null, "max.backups");
            int maxBackups = -1;
            if (maxBackupsStr != null) {
                try {
                    maxBackups = Integer.parseInt(maxBackupsStr);
                } catch (NumberFormatException e) {
                    monitor.invalidMaxBackups(fileName, maxBackupsStr);
                }
                if (maxBackups < 1) {
                    monitor.invalidMaxBackups(fileName, maxBackupsStr);
                    maxBackups = -1;
                }
            }
            return new FileAppenderDefinition(fileName, rollType, rollSize, maxBackups);
        } else {
            monitor.invalidRollType(fileName, rollType);
            return new FileAppenderDefinition(fileName);
        }
    }

}