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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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
