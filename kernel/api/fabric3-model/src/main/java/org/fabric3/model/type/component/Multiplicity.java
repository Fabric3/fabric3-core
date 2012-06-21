/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.model.type.component;

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
