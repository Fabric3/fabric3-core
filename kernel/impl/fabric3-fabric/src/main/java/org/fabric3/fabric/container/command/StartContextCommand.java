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
package org.fabric3.fabric.container.command;

import java.net.URI;

/**
 * Starts a context on a runtime.
 */
public class StartContextCommand implements Command {
    private URI uri;
    private boolean log;

    public StartContextCommand(URI uri, boolean log) {
        this.uri = uri;
        this.log = log;
    }

    public URI getUri() {
        return uri;
    }

    public boolean isLog() {
        return log;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StartContextCommand that = (StartContextCommand) o;

        return !(uri != null ? !uri.equals(that.uri) : that.uri != null);

    }

    public int hashCode() {
        return (uri != null ? uri.hashCode() : 0);
    }
}
