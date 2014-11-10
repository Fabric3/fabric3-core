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
package org.fabric3.spi.model.type.xsd;

import java.util.List;
import javax.xml.namespace.QName;

/**
 * Specialization of DataType for complex types from the XML Schema type system.
 */
public class XSDComplexType extends XSDType {
    private static final long serialVersionUID = 6325312345723762898L;
    private boolean sequence;
    private List<XSDType> sequenceTypes;

    public XSDComplexType(Class<?> physical, QName logical) {
        super(physical, logical);
    }

    /**
     * Constructor for complex types that contain an XSD sequence.
     *
     * @param physical      the physical type
     * @param logical       the logical type
     * @param sequenceTypes a collection of sequence types
     */
    public XSDComplexType(Class<?> physical, QName logical, List<XSDType> sequenceTypes) {
        super(physical, logical);
        this.sequenceTypes = sequenceTypes;
        sequence = true;
    }

    public boolean isSequence() {
        return sequence;
    }

    public List<XSDType> getSequenceTypes() {
        return sequenceTypes;
    }
}
