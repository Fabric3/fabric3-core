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
package org.fabric3.transform.property;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.TypeConstants;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.w3c.dom.Node;

/**
 *
 */
public class Property2FloatTransformer implements SingleTypeTransformer<Node, Float> {
    private static final JavaType TARGET = new JavaType(Float.class);

    public DataType getSourceType() {
        return TypeConstants.PROPERTY_TYPE;
    }

    public DataType getTargetType() {
        return TARGET;
    }

    public Float transform(Node node, ClassLoader loader) throws ContainerException {
        try {
            return Float.valueOf(node.getTextContent());
        } catch (NumberFormatException ex) {
            throw new ContainerException("Unsupportable float " + node.getTextContent(), ex);
        }
    }
}
