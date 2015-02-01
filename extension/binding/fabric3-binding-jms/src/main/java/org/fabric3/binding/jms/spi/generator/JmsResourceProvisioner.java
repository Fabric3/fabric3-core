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
package org.fabric3.binding.jms.spi.generator;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.jms.spi.provision.JmsConnectionSource;
import org.fabric3.binding.jms.spi.provision.JmsConnectionTarget;
import org.fabric3.binding.jms.spi.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.spi.provision.JmsWireTargetDefinition;

/**
 * Called by the JMS binding after generation has run to allow a host environment to provision JMS resources such as destinations. Implementations may
 * update generated definitions.
 * <p/>
 * Note this extension is optional.
 */
public interface JmsResourceProvisioner {

    /**
     * Called after a source definition has been generated.
     *
     * @param definition the source definition
     * @throws Fabric3Exception if an error occurs provisioning a required JMS artifact.
     */
    void generateSource(JmsWireSourceDefinition definition) throws Fabric3Exception;

    /**
     * Called after a target definition has been generated.
     *
     * @param definition the target definition
     * @throws Fabric3Exception if an error occurs provisioning a required JMS artifact.
     */
    void generateTarget(JmsWireTargetDefinition definition) throws Fabric3Exception;

    /**
     * Called after a source connection definition has been generated.
     *
     * @param definition the source definition
     * @throws Fabric3Exception if an error occurs provisioning a required JMS artifact.
     */
    public void generateConnectionSource(JmsConnectionSource definition) throws Fabric3Exception ;

    /**
     * Called after a target connection definition has been generated.
     *
     * @param definition the target definition
     * @throws Fabric3Exception if an error occurs provisioning a required JMS artifact.
     */
    public void generateConnectionTarget(JmsConnectionTarget definition) throws Fabric3Exception;
    
}
