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
package org.fabric3.binding.jms.spi.provision;

import java.net.URI;

import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 * Generated metadata used for attaching producers to a JMS destination.
 */
public class JmsConnectionTargetDefinition extends PhysicalConnectionTargetDefinition {
    private static final long serialVersionUID = -2617453498606879652L;
    private JmsBindingMetadata metadata;

    /**
     * Constructor.
     *
     * @param uri      the target URI
     * @param metadata metadata used to create a JMS message producer.
     * @param type     the data type events should be deserialized to
     */
    public JmsConnectionTargetDefinition(URI uri, JmsBindingMetadata metadata, DataType type) {
        super(type);
        this.metadata = metadata;
        setUri(uri);
    }

    public JmsBindingMetadata getMetadata() {
        return metadata;
    }

}