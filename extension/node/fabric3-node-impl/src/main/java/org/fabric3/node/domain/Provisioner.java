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

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;

/**
 * Deploys an instance as a component to the domain.
 */
public interface Provisioner {

    /**
     * Deploy the instance.
     *
     * @param name       the component name
     * @param instance   the instance
     * @param interfaces the service interfaces implemented by the instance
     * @throws DeploymentException if there is a deployment error
     */
    void deploy(String name, Object instance, Class<?>... interfaces) throws DeploymentException;

    /**
     * Deploys a composite.
     *
     * @param composite the composite
     * @throws DeploymentException if there is a deployment error
     */
    void deploy(Composite composite) throws DeploymentException;

    /**
     * Deploys a component corresponding to the given definition.
     *
     * @param definition the component definition
     * @throws DeploymentException if there is a deployment error
     */
    void deploy(Component<?> definition) throws DeploymentException;

    /**
     * Deploys a channel.
     *
     * @param definition the channel
     * @throws DeploymentException if there is a deployment error
     */
    void deploy(Channel definition) throws DeploymentException;

    /**
     * Un-deploys the channel or component with the given name
     *
     * @param name the channel or component name
     * @throws DeploymentException if there is an un-deployment error
     */
    void undeploy(String name) throws DeploymentException;

    /**
     * Un-deploys a composite.
     *
     * @param name the composite name
     * @throws DeploymentException if there is an un-deployment error
     */
    void undeploy(QName name) throws DeploymentException;
}
