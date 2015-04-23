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
package org.fabric3.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate an implementation method is an event consumer.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Consumer {

    /**
     * Denotes the name of the consumer declared by the implementation method.
     *
     * @return the name of the consumer declared by the implementation method
     */
    public abstract String value() default "";

    /**
     * Denotes the sequence in which a consumer should receive events from a channel.
     *
     * @return the sequence in which a consumer should receive events from a channel
     */
    public abstract int sequence() default 0;

    /**
     * Specifies the group the consumer is a member of.
     *
     * @return the group
     */
    public abstract String group() default "";

    /**
     * Returns the source channel for the consumer.
     *
     * @return the source channel for the consumer
     */
    public abstract String source() default "";

    /**
     * Returns the source channels for the consumer.
     *
     * @return the source channels for the consumer
     */
    public abstract String[] sources() default {};

}