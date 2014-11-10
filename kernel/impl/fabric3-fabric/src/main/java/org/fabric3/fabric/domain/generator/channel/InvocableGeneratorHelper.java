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
package org.fabric3.fabric.domain.generator.channel;

import java.net.URI;

import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalInvocable;

/**
 *
 */
public class InvocableGeneratorHelper {

    /**
     * Returns the channel in the invocable hierarchy.
     *
     * @param channelUri the channel uri
     * @param invocable  the invocable
     * @return the channel
     * @throws ChannelNotFoundException if the channel is not found
     */
    public static LogicalChannel getChannelInHierarchy(URI channelUri, LogicalInvocable invocable) throws ChannelNotFoundException {
        LogicalChannel channel = null;
        LogicalCompositeComponent parent = invocable.getParent().getParent();
        while (true) {
            if (parent != null) {
                channel = parent.getChannel(channelUri);
                if (channel != null) {
                    break;
                }
                parent = parent.getParent();
            } else {
                break;
            }
        }
        if (channel == null) {
            throw new ChannelNotFoundException("Channel not found: " + channelUri);
        }
        return channel;
    }

}
