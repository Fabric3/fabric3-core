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
package org.fabric3.node.domain;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
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
    void deploy(ComponentDefinition<?> definition) throws DeploymentException;

    /**
     * Deploys a channel.
     *
     * @param definition the channel
     * @throws DeploymentException if there is a deployment error
     */
    void deploy(ChannelDefinition definition) throws DeploymentException;

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
