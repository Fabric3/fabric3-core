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
package org.fabric3.monitor.impl.appender;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.runtime.HostInfo;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class AppenderFactoryImpl implements AppenderFactory {
    private static final long SIZE_100MB = 104857600;

    private HostInfo hostInfo;
    private AppenderFactoryMonitor monitor;

    public AppenderFactoryImpl(@Reference HostInfo hostInfo, @Monitor AppenderFactoryMonitor monitor) {
        this.hostInfo = hostInfo;
        this.monitor = monitor;
    }

    public List<Appender> instantiateDefaultAppenders() throws AppenderCreationException {
        return Collections.<Appender>singletonList(new ConsoleAppender());
    }

    public List<Appender> instantiate(XMLStreamReader reader) throws AppenderCreationException, XMLStreamException {
        List<Appender> appenders = new ArrayList<Appender>();
        Set<String> definedTypes = new HashSet<String>();

        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if ("appender".equals(reader.getName().getLocalPart())) {
                        String type = reader.getAttributeValue(null, "type");
                        if (type == null) {
                            throw new AppenderCreationException("Appender type not specified in system configuration " + getLocation(reader));
                        }
                        if (definedTypes.contains(type)) {
                            monitor.multipleAppenders(type);
                            continue;
                        }
                        definedTypes.add(type);
                        if ("console".equalsIgnoreCase(type)) {
                            ConsoleAppender appender = new ConsoleAppender();
                            appenders.add(appender);
                        } else if ("file".equalsIgnoreCase(type)) {
                            RollStrategy strategy;
                            String strategyType = reader.getAttributeValue(null, "roll.type");
                            if (strategyType == null || "none".equals(strategyType)) {
                                strategy = new NoRollStrategy();
                            } else if (strategyType.equals("size")) {
                                String sizeStr = reader.getAttributeValue(null, "roll.size");
                                long size = SIZE_100MB;
                                if (sizeStr != null) {
                                    try {
                                        size = Long.parseLong(sizeStr);
                                    } catch (NumberFormatException e) {
                                        throw new AppenderCreationException("Invalid roll size specified in system configuration " + getLocation(reader), e);
                                    }
                                }
                                strategy = new SizeRollStrategy(size);
                            } else {
                                throw new AppenderCreationException(
                                        "Invalid roll type '" + strategyType + "' specified in system configuration " + getLocation(reader));
                            }

                            File outputDir = new File(hostInfo.getDataDir(), "logs");
                            outputDir.mkdirs();
                            File outputFile = new File(outputDir, "fabric3.log");
                            RollingFileAppender appender = new RollingFileAppender(outputFile, strategy);
                            appenders.add(appender);
                        } else {
                            throw new AppenderCreationException(
                                    "Unknown appender type '" + type + "' specified in system configuration " + getLocation(reader));
                        }

                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("appenders".equals(reader.getName().getLocalPart())) {
                        return appenders;
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    throw new AssertionError("End of document encountered");

            }
        }
    }

    private String getLocation(XMLStreamReader reader) {
        Location location = reader.getLocation();
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        return "[" + line + ", " + "" + col + "]";
    }
}
