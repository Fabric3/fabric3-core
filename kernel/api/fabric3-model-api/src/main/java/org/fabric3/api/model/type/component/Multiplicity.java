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
package org.fabric3.api.model.type.component;

/**
 * Enumeration for multiplicity.
 */
public enum Multiplicity {
    /**
     * Indicates a relationship that is optionally connected to the requestor and which, if supplied, must be connected to exactly one provider.
     */
    ZERO_ONE("0..1"),

    /**
     * Indicates a relationship that must be connected between exactly one requestor and exactly one provider.
     */
    ONE_ONE("1..1"),

    /**
     * Indicates a relationship that is optionally connects the requestor to zero to unbounded providers.
     */
    ZERO_N("0..n"),

    /**
     * Indicates a relationship that must be connected at the requestor and which connects it to zero to unbounded providers.
     */
    ONE_N("1..n");

    private final String text;

    Multiplicity(String value) {
        this.text = value;
    }

    /**
     * Returns the textual form of Multiplicity as defined by the Assembly spec.
     *
     * @return the textual form of Multiplicity as defined by the Assembly spec
     */
    public String toString() {
        return text;
    }

    /**
     * Parse the text form as defined by the Assembly spec.
     *
     * @param text multiplicity value as text as described by the Assembly spec; may be null
     * @return the value corresponding to the text, or null if text is null
     * @throws IllegalArgumentException if the text is not a valid value
     */
    public static Multiplicity fromString(String text) throws IllegalArgumentException {
        if (text == null) {
            return null;
        }

        for (Multiplicity multiplicity : Multiplicity.values()) {
            if (multiplicity.text.equals(text)) {
                return multiplicity;
            }
        }
        throw new IllegalArgumentException(text);
    }
}
