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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.container.builder.component;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;

/**
 * Attaches and detaches a {@link ChannelConnection} from a component producer, channel or channel binding.
 */
public interface SourceConnectionAttacher<P extends PhysicalConnectionSource> {

    /**
     * Attach a connection to a component producer or channel binding transport.
     *
     * @param source     the source metadata
     * @param target     the target metadata
     * @param connection the channel connection
     * @throws Fabric3Exception if an error is encountered performing the attach
     */
    void attach(P source, PhysicalConnectionTarget target, ChannelConnection connection) throws Fabric3Exception;

    /**
     * Detach a connection from a source.
     *
     * @param source the source metadata
     * @param target the target metadata
     * @throws Fabric3Exception if an error is encountered performing the attach
     */
    default void detach(P source, PhysicalConnectionTarget target) throws Fabric3Exception {

    }

}