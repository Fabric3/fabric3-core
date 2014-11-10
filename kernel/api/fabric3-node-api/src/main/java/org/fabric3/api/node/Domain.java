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
package org.fabric3.api.node;

import javax.xml.namespace.QName;
import java.net.URL;

import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;

/**
 * The main API for accessing a service fabric domain.
 */
public interface Domain {

    /**
     * Returns a proxy to the service with the given interface. The service may be local or remote depending on the deployment topology.
     *
     * @param interfaze the service interface.
     * @return the service
     * @throws NotFoundException if the service is not found.
     */
    <T> T getService(Class<T> interfaze);

    /**
     * Returns a proxy to a channel.
     *
     * @param interfaze the channel interface
     * @param name      the channel name
     * @return the channel
     * @throws NotFoundException if the channel is not found.
     */
    <T> T getChannel(Class<T> interfaze, String name);

    /**
     * Deploys a composite.
     *
     * @param composite the composite
     * @return the domain
     */
    Domain deploy(Composite composite);

    /**
     * Deploys a component.
     *
     * @param name       the component name
     * @param instance   the service instance
     * @param interfaces the service endpoint interfaces provided by the component
     * @return the domain
     */
    Domain deploy(String name, Object instance, Class<?>... interfaces);

    /**
     * Deploys a component specified by the given definition.
     *
     * @param definition the component definition
     * @return the domain
     */
    Domain deploy(ComponentDefinition<?> definition);

    /**
     * Deploys a channel.
     *
     * @param definition the channel
     */
    Domain deploy(ChannelDefinition definition);

    /**
     * Deploys an artifact such as a composite file or contribution to the domain.
     *
     * @param url the artifact URL
     * @return the domain
     */
    Domain deploy(URL url);

    /**
     * Un-deploys a composite.
     *
     * @param name the composite name
     * @return the domain
     */
    Domain undeploy(QName name);

    /**
     * Un-deploys the channel or component with the given name
     *
     * @param name the component name
     * @return the domain
     */
    Domain undeploy(String name);

    /**
     * Un-deploys an artifact such as a composite file or contribution from the domain.
     *
     * @param url the artifact URL
     * @return the domain
     */
    Domain undeploy(URL url);

}
