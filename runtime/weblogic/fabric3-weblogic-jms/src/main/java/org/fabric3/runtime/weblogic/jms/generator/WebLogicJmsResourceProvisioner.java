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
package org.fabric3.runtime.weblogic.jms.generator;

import org.fabric3.api.host.ContainerException;
import org.fabric3.binding.jms.spi.generator.JmsResourceProvisioner;
import org.fabric3.binding.jms.spi.provision.JmsConnectionSource;
import org.fabric3.binding.jms.spi.provision.JmsConnectionTarget;
import org.fabric3.binding.jms.spi.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.spi.provision.JmsWireTargetDefinition;

/**
 *
 */
public class WebLogicJmsResourceProvisioner implements JmsResourceProvisioner {

    public void generateSource(JmsWireSourceDefinition definition) throws ContainerException {
        // TODO implement
    }

    public void generateTarget(JmsWireTargetDefinition definition) throws ContainerException {
        // TODO implement
    }

    public void generateConnectionSource(JmsConnectionSource definition) throws ContainerException {
        // TODO implement
    }

    public void generateConnectionTarget(JmsConnectionTarget definition) throws ContainerException {
        // TODO implement
    }

}
