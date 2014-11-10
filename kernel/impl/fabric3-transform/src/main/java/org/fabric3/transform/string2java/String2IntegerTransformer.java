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
package org.fabric3.transform.string2java;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.spi.transform.TransformationException;

/**
 *
 */
public class String2IntegerTransformer implements SingleTypeTransformer<String, Integer> {
    private static final XSDSimpleType SOURCE = new XSDSimpleType(String.class, new QName(XSDType.XSD_NS, "string"));
    private static final JavaType TARGET = new JavaType(Integer.class);

    public DataType getSourceType() {
        return SOURCE;
    }

    public DataType getTargetType() {
        return TARGET;
    }

    public Integer transform(String source, ClassLoader loader) throws TransformationException {
        try {
            return Integer.valueOf(source);
        } catch (NumberFormatException e) {
            throw new TransformationException(e);
        }
    }

}