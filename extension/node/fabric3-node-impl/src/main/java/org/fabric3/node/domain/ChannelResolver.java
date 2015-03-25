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
package org.fabric3.node.domain;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Resolves a channel and returns a connection proxy to it.
 */
public interface ChannelResolver {

    /**
     * Creates a producer proxy to a channel using the default topic.
     *
     * @param interfaze the producer interface
     * @param name      the channel name
     * @return the connection proxy
     * @throws Fabric3Exception if there is a resolution exception
     */
    <T> T getProducer(Class<T> interfaze, String name) throws Fabric3Exception;

    /**
     * Creates a producer proxy to a channel using the default topic.
     *
     * @param interfaze the producer interface
     * @param name      the channel name
     * @param topic     the topic name
     * @return the connection proxy
     * @throws Fabric3Exception if there is a resolution exception
     */
    <T> T getProducer(Class<T> interfaze, String name, String topic);
}
