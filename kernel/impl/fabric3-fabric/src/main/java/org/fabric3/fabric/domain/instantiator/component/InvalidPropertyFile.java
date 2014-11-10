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
package org.fabric3.fabric.domain.instantiator.component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;

import org.fabric3.api.host.failure.AssemblyFailure;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 *
 */
public class InvalidPropertyFile extends AssemblyFailure {
    private String name;
    private Throwable cause;
    private final URI file;

    public InvalidPropertyFile(String name, LogicalComponent component, Throwable cause, URI file) {
        super(component.getUri(), component.getDefinition().getContributionUri(), Collections.singletonList(component));
        this.name = name;
        this.cause = cause;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public Throwable getCause() {
        return cause;
    }

    public URI getFile() {
        return file;
    }

    public String getMessage() {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        cause.printStackTrace(pw);
        return "The property file for property " + name + " in component " + getComponentUri()
                + " is invalid due to an error processing the file  " + file + ". The error thrown was: \n" + writer;
    }
}
