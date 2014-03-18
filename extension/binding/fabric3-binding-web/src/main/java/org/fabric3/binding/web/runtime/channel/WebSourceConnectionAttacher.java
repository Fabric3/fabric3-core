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
package org.fabric3.binding.web.runtime.channel;

import java.net.URI;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.binding.web.provision.WebConnectionSourceDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Reference;

/**
 * No-op attacher.
 */
public class WebSourceConnectionAttacher implements SourceConnectionAttacher<WebConnectionSourceDefinition> {
    private TransformerRegistry transformerRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    public WebSourceConnectionAttacher(@Reference TransformerRegistry transformerRegistry, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.transformerRegistry = transformerRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(WebConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ContainerException {
        EventStream eventStream = connection.getEventStream();
        PhysicalEventStreamDefinition streamDefinition = eventStream.getDefinition();
        URI classLoaderUri = source.getClassLoaderId();
        ClassLoader loader = classLoaderRegistry.getClassLoader(classLoaderUri);
        List<String> eventTypes = streamDefinition.getEventTypes();
        String stringifiedType = eventTypes.get(0);
        try {
            DataType type = new JavaType(loader.loadClass(stringifiedType));
            TransformerHandler handler = new TransformerHandler(type, transformerRegistry);
            eventStream.addHandler(handler);
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }

    public void detach(WebConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) throws ContainerException {
    }

    /**
     * Adds event transformers to convert an event from one format to another.
     *
     * @param streamDefinition the stream definition
     * @param stream           the stream being created
     * @param loader           the target classloader to use for the transformation
     * @throws ContainerException if there is an error adding a filter
     */
    @SuppressWarnings({"unchecked"})
    private void addTransformer(PhysicalEventStreamDefinition streamDefinition, EventStream stream, ClassLoader loader) throws ContainerException {
    }

}
