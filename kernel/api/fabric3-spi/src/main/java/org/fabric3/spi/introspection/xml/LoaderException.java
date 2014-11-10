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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.introspection.xml;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.introspection.IntrospectionException;

/**
 * Base class for Exceptions raised during the loading process. Loader implementations should throw a subclass of this to indicate the actual
 * problem.
 */
public class LoaderException extends IntrospectionException {
    public static final int UNDEFINED = -1;
    private static final long serialVersionUID = -7459051598906813461L;
    private final String resourceURI;
    private final int line;
    private final int column;

    public LoaderException(String message, XMLStreamReader reader) {
        super(message);
        Location location = reader.getLocation();
        if (location != null) {
            line = location.getLineNumber();
            column = location.getColumnNumber();
            resourceURI = location.getSystemId();
        } else {
            resourceURI = "system";
            line = -1;
            column = -1;
        }
    }

    public LoaderException(String message, Throwable cause) {
        super(message, cause);
        line = UNDEFINED;
        column = UNDEFINED;
        resourceURI = null;
    }

    /**
     * Returns the location of the resource that was being loaded.
     *
     * @return the location of the resource that was being loaded
     */
    public String getResourceURI() {
        return resourceURI;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(" in ");
        builder.append(resourceURI == null ? "unknown" : resourceURI);
        if (line != -1) {
            builder.append(" at ").append(line).append(',').append(column);
        }
        builder.append(": ");
        builder.append(getLocalizedMessage());
        return builder.toString();
    }
}
