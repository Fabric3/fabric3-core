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
package org.fabric3.implementation.spring.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Metadata for attaching a wire to a target Spring bean in a Spring application context.
 */
public class SpringWireTargetDefinition extends PhysicalWireTargetDefinition {
    private static final long serialVersionUID = 825961411627210578L;

    private String beanName;
    private String beanInterface;

    public SpringWireTargetDefinition(String beanName, String beanInterface, URI uri) {
        this.beanName = beanName;
        this.beanInterface = beanInterface;
        setUri(uri);
    }

    public String getBeanName() {
        return beanName;
    }

    public String getBeanInterface() {
        return beanInterface;
    }
}