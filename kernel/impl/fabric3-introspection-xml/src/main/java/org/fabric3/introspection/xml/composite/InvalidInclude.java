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
package org.fabric3.introspection.xml.composite;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.stream.Location;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.xml.XmlValidationFailure;

/**
 *
 */
public class InvalidInclude extends XmlValidationFailure {
    private Throwable cause;

    public InvalidInclude(String message, Location location, ModelObject... sources) {
        super(message, location, sources);
    }

    public InvalidInclude(String message, Throwable cause, Location location, ModelObject... sources) {
        super(message, location, sources);
        this.cause = cause;
    }

    public String getMessage() {
        if (cause != null) {
            StringWriter writer = new StringWriter();
            writer.write(super.getMessage() + ". The original error was: \n");
            cause.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        } else {
            return super.getMessage();
        }
    }

    public String getShortMessage() {
        if (cause != null) {
            return getMessage() + ": " + cause.getMessage();
        } else {
            return super.getShortMessage();
        }
    }

}