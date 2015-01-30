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
package org.fabric3.jpa.override;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.ExtensionsInitialized;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Parses persistence entries specified in the system configuration.
 */
@EagerInit
@Management
public class SystemConfigOverrideParser implements Fabric3EventListener<ExtensionsInitialized> {
    private LoaderRegistry loaderRegistry;
    private EventService eventService;
    private OverrideParserMonitor monitor;
    private XMLStreamReader reader;

    public SystemConfigOverrideParser(@Reference LoaderRegistry loaderRegistry,
                                      @Reference EventService eventService,
                                      @Monitor OverrideParserMonitor monitor) {
        this.loaderRegistry = loaderRegistry;
        this.eventService = eventService;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setOverrideConfig(XMLStreamReader reader) {
        this.reader = reader;
    }

    @Init
    public void init() {
        eventService.subscribe(ExtensionsInitialized.class, this);
    }

    @Destroy
    public void destroy() {
        eventService.unsubscribe(ExtensionsInitialized.class, this);
    }

    public void onEvent(ExtensionsInitialized event) {
        // delay parsing until runtime extensions have been loaded as the latter may be needed to parse template elements
        if (reader == null) {
            // property was not set, return
            return;
        }
        try {
            reader.nextTag(); // skip to first tag
            reader.nextTag(); // skip past value tag
            ClassLoader classLoader = getClass().getClassLoader();
            DefaultIntrospectionContext context = new DefaultIntrospectionContext(Names.HOST_CONTRIBUTION, classLoader, null);
            loaderRegistry.load(reader, ModelObject.class, context);
            if (context.hasErrors()) {
                // TODO abort registration and log
                List<ValidationFailure> errors = context.getErrors();
                for (ValidationFailure error : errors) {
                    monitor.parseError(error.getMessage());
                }
            }
        } catch (XMLStreamException e) {
            monitor.error(e);
        } finally {
            close();
        }
    }

    private void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                e.printStackTrace();
                // ignore
            }
            reader = null;
        }
    }
}
