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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.spring.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.type.java.JavaType;

/**
 * Used to attach the target side of a channel connection to a Spring bean consumer.
 */
public class SpringConnectionTargetDefinition extends PhysicalConnectionTargetDefinition {
    private static final long serialVersionUID = 967914855829805771L;
    private String beanName;
    private JavaType type;
    private String methodName;

    /**
     * Constructor.
     *
     * @param beanName   the name of the bean to dispatch events to
     * @param methodName the name of the bean method to dispatch events to
     * @param type       the event type
     * @param channelUri the URI of the source channel
     */
    public SpringConnectionTargetDefinition(String beanName, String methodName, JavaType type, URI channelUri) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.type = type;
        setUri(channelUri);
    }

    public String getBeanName() {
        return beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public JavaType getType() {
        return type;
    }

}