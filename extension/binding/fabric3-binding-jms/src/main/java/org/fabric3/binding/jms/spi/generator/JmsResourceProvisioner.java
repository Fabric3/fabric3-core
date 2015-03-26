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
import org.fabric3.binding.jms.spi.provision.JmsWireSource;
import org.fabric3.binding.jms.spi.provision.JmsWireTarget;

/**
 * Called by the JMS binding after generation has run to allow a host environment to provision JMS resources such as destinations. Implementations may update
 * generated definitions.
 *
 * Note this extension is optional.
 */
public interface JmsResourceProvisioner {

    /**
     * Called after a source has been generated.
     *
     * @param source the source definition
     * @throws Fabric3Exception if an error occurs provisioning a required JMS artifact.
     */
    void generateSource(JmsWireSource source) throws Fabric3Exception;

    /**
     * Called after a target has been generated.
     *
     * @param target the target definition
     * @throws Fabric3Exception if an error occurs provisioning a required JMS artifact.
     */
    void generateTarget(JmsWireTarget target) throws Fabric3Exception;

    /**
     * Called after a source connection has been generated.
     *
     * @param source the source
     * @throws Fabric3Exception if an error occurs provisioning a required JMS artifact.
     */
    public void generateConnectionSource(JmsConnectionSource source) throws Fabric3Exception;

    /**
     * Called after a target connection has been generated.
     *
     * @param target the target
     * @throws Fabric3Exception if an error occurs provisioning a required JMS artifact.
     */
    public void generateConnectionTarget(JmsConnectionTarget target) throws Fabric3Exception;

}
