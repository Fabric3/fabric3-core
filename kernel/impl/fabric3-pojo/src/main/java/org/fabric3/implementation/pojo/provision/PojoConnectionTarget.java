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
package org.fabric3.implementation.pojo.provision;

import java.lang.reflect.AccessibleObject;

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;

/**
 *
 */
public class PojoConnectionTarget extends PhysicalConnectionTarget {
    private AccessibleObject consumer;
    private Injectable injectable;

    public AccessibleObject getConsumerObject() {
        return consumer;
    }

    public void setConsumerSite(AccessibleObject consumer) {
        this.consumer = consumer;
    }

    public void setInjectable(Injectable injectable) {
        this.injectable = injectable;
    }

    public Injectable getInjectable() {
        return injectable;
    }

}