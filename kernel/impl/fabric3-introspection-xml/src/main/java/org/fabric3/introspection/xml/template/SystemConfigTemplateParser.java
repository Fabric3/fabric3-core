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
package org.fabric3.introspection.xml.template;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.ExtensionsInitialized;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.xml.LocationAwareXMLStreamReader;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Parses template entries specified in the system configuration.
 */
@EagerInit
@Management
public class SystemConfigTemplateParser implements Fabric3EventListener<ExtensionsInitialized> {
    private LoaderRegistry loaderRegistry;
    private EventService eventService;
    private TemplateParserMonitor monitor;
    private XMLStreamReader reader;

    public SystemConfigTemplateParser(@Reference LoaderRegistry loaderRegistry,
                                      @Reference EventService eventService,
                                      @Monitor TemplateParserMonitor monitor) {
        this.loaderRegistry = loaderRegistry;
        this.eventService = eventService;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setTemplateConfig(XMLStreamReader reader) {
        this.reader = new LocationAwareXMLStreamReader(reader, "system configuration");
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
        // delay template parsing until runtime extensions have been loaded as the latter may be needed to parse template elements
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
