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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.fabric3.api.model.type.contract.DataType;

/**
 * Serves as the root of complex and simple types from the XML Schema type system.
 */
public class XSDType extends DataType {
    private static final long serialVersionUID = 4837060732513291971L;
    public static final String XSD_NS = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    public XSDType(Class<?> type, QName xsdType) {
        super(type, xsdType);
    }

    public XSDType(Class<String> type, QName xsdType, String databinding) {
        super(type, xsdType);
        setDatabinding(databinding);
    }
}
