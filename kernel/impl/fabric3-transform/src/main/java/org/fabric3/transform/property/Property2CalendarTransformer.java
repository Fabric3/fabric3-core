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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.xsd.XSDConstants;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.spi.transform.TransformationException;
import org.w3c.dom.Node;

/**
 * Transforms from a DOM representation to a Calendar, expecting the format of the date to correspond to XML Schema (2007-10-31T01:02:03Z)
 */
public class Property2CalendarTransformer implements SingleTypeTransformer<Node, Calendar> {
    private static final JavaType TARGET = new JavaType(Calendar.class);

    private final DatatypeFactory factory;

    public DataType getSourceType() {
        return XSDConstants.PROPERTY_TYPE;
    }

    public Property2CalendarTransformer() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    public DataType getTargetType() {
        return TARGET;
    }

    public Calendar transform(final Node node, ClassLoader loader) throws TransformationException {
        XMLGregorianCalendar xmlCalendar = factory.newXMLGregorianCalendar(node.getTextContent());
        return xmlCalendar.toGregorianCalendar();
    }

}