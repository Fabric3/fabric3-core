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
 */
package org.fabric3.api.model.type.builder;

import org.fabric3.api.model.type.component.Target;
import org.fabric3.api.model.type.component.Wire;

/**
 * Builds a wire definition.
 */
public class WireBuilder extends AbstractBuilder {
    private Target reference;
    private Target service;

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static WireBuilder newBuilder() {
        return new WireBuilder();
    }

    /**
     * Sets the reference source.
     *
     * @param value the reference in the form component/reference/binding where reference and binding may be optional
     * @return the builder
     */
    public WireBuilder source(String value) {
        checkState();
        reference = parseTarget(value);
        return this;
    }

    /**
     * Sets the service target.
     *
     * @param value the reference in the form component/service/binding where reference and binding may be optional
     * @return the builder
     */
    public WireBuilder target(String value) {
        checkState();
        service = parseTarget(value);
        return this;
    }

    /**
     * Builds the wire.
     *
     * @return the built wire
     */
    public Wire build() {
        checkState();
        freeze();
        return new Wire(reference, service, true);
    }

    protected WireBuilder() {
    }

    private Target parseTarget(String target) {
        String[] tokens = target.split("/");
        if (tokens.length == 1) {
            return new Target(tokens[0]);
        } else if (tokens.length == 2) {
            return new Target(tokens[0], tokens[1]);
        } else if (tokens.length == 3) {
            return new Target(tokens[0], tokens[1], tokens[2]);
        } else {
            throw new IllegalArgumentException("Invalid target format: " + target);

        }
    }
}
