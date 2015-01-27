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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.TypeConstants;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.spi.transform.TransformationException;
import org.w3c.dom.Node;

/**
 * Transformer that converts from a DOM Node to a Java Date, expecting the format of the date to be yyy-MM-d'T'HH:mm:ss'Z'.
 */
public class Property2DateTransformer implements SingleTypeTransformer<Node, Date> {
    private static final JavaType TARGET = new JavaType(Date.class);

    private final DateFormat dateFormatter;

    public DataType getSourceType() {
        return TypeConstants.PROPERTY_TYPE;
    }

    public DataType getTargetType() {
        return TARGET;
    }

    public Property2DateTransformer() {
        dateFormatter = new SimpleDateFormat("yyy-MM-d'T'HH:mm:ss'Z'");
        dateFormatter.setLenient(false);
    }


    public Date transform(final Node node, ClassLoader loader) throws TransformationException {
        try {
            return dateFormatter.parse(node.getTextContent());
        } catch (ParseException pe) {
            throw new TransformationException("Unsupported Date Format ", pe);
        }
    }

}
