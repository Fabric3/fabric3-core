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
package org.fabric3.fabric.domain.instantiator;

import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Instantiates explicit wires, i.e. those declared by a <code>wire<code> wire element or using the <code>target</code> attribute on a
 * <code>reference</code> element. During instantiation, wires will be validated.
 *
 * Note {@link #instantiateCompositeWires(Composite, LogicalCompositeComponent, InstantiationContext)} must be called before {@link
 * #instantiateReferenceWires(LogicalComponent, InstantiationContext)} as composite <code>&lt;wire&gt;</code> elements may use the @replace attribute
 * to overwrite wires created using the @target attribute on a <code>&lt;reference&gt;</code> element. If composite wires with replace set to true
 * exist, wires base on the reference target attribute will not be created. 
 */
public interface WireInstantiator {

    /**
     * Instantiates wires declared using a <code>wire</code> element in a composite.
     *
     * @param composite the composite
     * @param parent    the logical composite where the wires will be added
     * @param context   the instantiation context.
     */
    void instantiateCompositeWires(Composite composite, LogicalCompositeComponent parent, InstantiationContext context);

    /**
     * Instantiates wires declared using the <code>target</code> attribute on a <code>reference</code> element.
     *
     * @param component the logical component containing the configured references
     * @param context   the instantiation context.
     */
    void instantiateReferenceWires(LogicalComponent<?> component, InstantiationContext context);
}
