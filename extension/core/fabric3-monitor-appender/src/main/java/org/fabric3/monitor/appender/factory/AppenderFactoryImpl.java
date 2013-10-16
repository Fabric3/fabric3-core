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
package org.fabric3.monitor.appender.factory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.failure.ValidationFailure;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.monitor.appender.console.ConsoleAppender;
import org.fabric3.monitor.appender.console.ConsoleAppenderDefinition;
import org.fabric3.monitor.spi.appender.AppenderCreationException;
import org.fabric3.monitor.spi.appender.AppenderFactory;
import org.fabric3.monitor.spi.model.type.AppenderDefinition;
import org.fabric3.monitor.spi.model.physical.PhysicalAppenderDefinition;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderBuilder;
import org.fabric3.monitor.spi.appender.AppenderGenerator;
import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class AppenderFactoryImpl implements AppenderFactory {
    private LoaderRegistry registry;
    private AppenderFactoryMonitor monitor;

    private Map<Class<?>, AppenderGenerator<?>> appenderGenerators;
    private Map<Class<?>, AppenderBuilder<?>> appenderBuilders;

    public AppenderFactoryImpl(@Reference LoaderRegistry registry, @Monitor AppenderFactoryMonitor monitor) {
        this.registry = registry;
        this.monitor = monitor;
    }

    @Reference
    public void setAppenderGenerators(Map<Class<?>, AppenderGenerator<?>> appenderGenerators) {
        this.appenderGenerators = appenderGenerators;
    }

    @Reference
    public void setAppenderBuilders(Map<Class<?>, AppenderBuilder<?>> appenderBuilders) {
        this.appenderBuilders = appenderBuilders;
    }

    public List<Appender> instantiateDefaultAppenders() throws AppenderCreationException {
        return Collections.<Appender>singletonList(new ConsoleAppender());
    }

    public List<Appender> instantiate(XMLStreamReader reader) throws AppenderCreationException, XMLStreamException {
        List<AppenderDefinition> definitions = load(reader);
        List<PhysicalAppenderDefinition> physicalDefinitions = generate(definitions);
        return build(definitions, physicalDefinitions);
    }

    private List<AppenderDefinition> load(XMLStreamReader reader) throws AppenderCreationException, XMLStreamException {
        List<AppenderDefinition> appenderDefinitions = new ArrayList<AppenderDefinition>();
        Set<String> definedTypes = new HashSet<String>();

        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (reader.getName().getLocalPart().startsWith("appender.")) {
                        IntrospectionContext context = new DefaultIntrospectionContext();
                        ModelObject modelObject = registry.load(reader, ModelObject.class, context);
                        if (context.hasErrors()) {
                            monitor.configurationError();
                            for (ValidationFailure error : context.getErrors()) {
                                monitor.configurationErrorDetail("Error reported: " + error.getMessage());
                            }
                            if (!definedTypes.contains(ConsoleAppenderDefinition.TYPE)) {
                                ConsoleAppenderDefinition appenderDefinition = new ConsoleAppenderDefinition();
                                appenderDefinitions.add(appenderDefinition);
                            }
                            return appenderDefinitions;
                        }
                        if (modelObject instanceof AppenderDefinition) {
                            AppenderDefinition appenderDefinition = (AppenderDefinition) modelObject;
                            if (definedTypes.contains(appenderDefinition.getType())) {
                                monitor.multipleAppenders(appenderDefinition.getType());
                                continue;
                            }
                            definedTypes.add(appenderDefinition.getType());
                            appenderDefinitions.add(appenderDefinition);

                        } else {
                            throw new AppenderCreationException("Unexpected type: " + modelObject);
                        }
                    }

                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("appenders".equals(reader.getName().getLocalPart())) {
                        return appenderDefinitions;
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    throw new AssertionError("End of document encountered");
            }

        }
    }

    @SuppressWarnings("unchecked")
    private List<PhysicalAppenderDefinition> generate(List<AppenderDefinition> definitions) throws AppenderCreationException {
        List<PhysicalAppenderDefinition> physicalDefinitions = new ArrayList<PhysicalAppenderDefinition>(definitions.size());
        for (AppenderDefinition definition : definitions) {
            AppenderGenerator generator = appenderGenerators.get(definition.getClass());
            if (generator == null) {
                throw new AppenderCreationException("Unknown appender type: " + definition.getClass());
            }
            try {
                PhysicalAppenderDefinition physicalDefinition = generator.generateResource(definition);
                physicalDefinitions.add(physicalDefinition);
            } catch (GenerationException e) {
                throw new AppenderCreationException(e);
            }
        }
        return physicalDefinitions;
    }

    @SuppressWarnings("unchecked")
    private List<Appender> build(List<AppenderDefinition> definitions, List<PhysicalAppenderDefinition> physicalDefinitions) throws AppenderCreationException {
        List<Appender> appenders = new ArrayList<Appender>(definitions.size());
        for (PhysicalAppenderDefinition definition : physicalDefinitions) {
            AppenderBuilder builder = appenderBuilders.get(definition.getClass());
            if (builder == null) {
                throw new AppenderCreationException("Unknown appender type: " + definition.getClass());
            }
            try {
                Appender appender = builder.build(definition);
                appenders.add(appender);
            } catch (BuilderException e) {
                throw new AppenderCreationException(e);
            }
        }
        return appenders;
    }

}
