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
package org.fabric3.spi.contribution;

import java.io.Serializable;

/**
 * A runtime capability required or provided by the contribution.
 */
public class Capability implements Serializable {
    private static final long serialVersionUID = 2123506244287635868L;
    private String name;
    private boolean load;

    public Capability(String name) {
        this.name = name;
    }

    public Capability(String name, boolean load) {
        this.name = name;
        this.load = load;
    }

    public String getName() {
        return name;
    }

    public boolean requiresLoad() {
        return load;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Capability) && name.equals(((Capability) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
