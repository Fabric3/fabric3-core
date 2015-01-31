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
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.monitor.appender.console.ConsoleAppender;
import org.fabric3.monitor.appender.console.ConsoleAppenderDefinition;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderBuilder;
import org.fabric3.monitor.spi.appender.AppenderFactory;
import org.fabric3.monitor.spi.appender.AppenderGenerator;
import org.fabric3.monitor.spi.model.physical.PhysicalAppenderDefinition;
import org.fabric3.monitor.spi.model.type.AppenderDefinition;
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

    public List<Appender> instantiateDefaultAppenders() throws ContainerException {
        return Collections.<Appender>singletonList(new ConsoleAppender());
    }

    public List<Appender> instantiate(XMLStreamReader reader) throws ContainerException, XMLStreamException {
        List<AppenderDefinition> definitions = load(reader);
        List<PhysicalAppenderDefinition> physicalDefinitions = generate(definitions);
        return build(definitions, physicalDefinitions);
    }

    private List<AppenderDefinition> load(XMLStreamReader reader) throws ContainerException, XMLStreamException {
        List<AppenderDefinition> appenderDefinitions = new ArrayList<>();
        Set<String> definedTypes = new HashSet<>();

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
                            throw new ContainerException("Unexpected type: " + modelObject);
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
    private List<PhysicalAppenderDefinition> generate(List<AppenderDefinition> definitions) throws ContainerException {
        List<PhysicalAppenderDefinition> physicalDefinitions = new ArrayList<>(definitions.size());
        for (AppenderDefinition definition : definitions) {
            AppenderGenerator generator = appenderGenerators.get(definition.getClass());
            if (generator == null) {
                throw new ContainerException("Unknown appender type: " + definition.getClass());
            }
            PhysicalAppenderDefinition physicalDefinition = generator.generateResource(definition);
            physicalDefinitions.add(physicalDefinition);
        }
        return physicalDefinitions;
    }

    @SuppressWarnings("unchecked")
    private List<Appender> build(List<AppenderDefinition> definitions, List<PhysicalAppenderDefinition> physicalDefinitions) throws ContainerException {
        List<Appender> appenders = new ArrayList<>(definitions.size());
        for (PhysicalAppenderDefinition definition : physicalDefinitions) {
            AppenderBuilder builder = appenderBuilders.get(definition.getClass());
            if (builder == null) {
                throw new ContainerException("Unknown appender type: " + definition.getClass());
            }
            Appender appender = builder.build(definition);
            appenders.add(appender);
        }
        return appenders;
    }

}
