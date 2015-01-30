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
package org.fabric3.spi.introspection.xml;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.model.type.ModelObject;

/**
 * Base class for validation failures occurring in XML artifacts.
 */
public abstract class XmlValidationFailure extends ValidationFailure {
    private int line = -1;
    private int column = -1;
    private int offset = -1;
    private String resourceURI = "system";
    private String message;

    private List<Object> sources;

    protected XmlValidationFailure(String message, Location location, ModelObject... sources) {
        this.message = message;
        if (location != null) {
            line = location.getLineNumber();
            column = location.getColumnNumber();
            offset = location.getCharacterOffset();
            resourceURI = location.getSystemId();
        }
        this.sources = new ArrayList<>();
        if (sources != null) {
            this.sources.addAll(Arrays.asList(sources));
        }
    }

    protected XmlValidationFailure(String message) {
        this(message, null);
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getOffset() {
        return offset;
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public List<Object> getSources() {
        return sources;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append(message);
        if (line != -1) {
            builder.append(" [").append(line).append(',').append(column).append("]");
        }
        return builder.toString();
    }

    public String getShortMessage() {
        return message;
    }

}
