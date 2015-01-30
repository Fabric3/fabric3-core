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
package org.fabric3.introspection.java.annotation;

import org.fabric3.api.annotation.wire.Order;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;

/**
 * Processes the {@link Order} annotation on component implementation classes.
 */
public class OrderProcessor extends AbstractAnnotationProcessor<Order> {

    public OrderProcessor() {
        super(Order.class);
    }

    public void visitType(Order annotation, Class<?> type, InjectingComponentType componentType, IntrospectionContext context) {
        if (annotation.value() == Integer.MIN_VALUE) {
            context.addError(new InvalidAnnotation("A value must be specified for @Order", type, annotation, type));
            return;
        }
        componentType.setOrder(annotation.value());
    }
}
