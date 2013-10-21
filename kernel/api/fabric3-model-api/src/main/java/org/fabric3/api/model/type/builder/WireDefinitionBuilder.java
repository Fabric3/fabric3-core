/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.api.model.type.builder;

import org.fabric3.api.model.type.component.Target;
import org.fabric3.api.model.type.component.WireDefinition;

/**
 * Builds a wire definition.
 */
public class WireDefinitionBuilder extends AbstractBuilder {
    private Target reference;
    private Target service;

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static WireDefinitionBuilder newBuilder() {
        return new WireDefinitionBuilder();
    }

    /**
     * Sets the reference.
     *
     * @param value the reference in the form component/reference/binding where reference and binding may be optional
     * @return the builder
     */
    public WireDefinitionBuilder reference(String value) {
        checkState();
        reference = parseTarget(value);
        return this;
    }

    /**
     * Sets the service.
     *
     * @param value the reference in the form component/service/binding where reference and binding may be optional
     * @return the builder
     */
    public WireDefinitionBuilder service(String value) {
        checkState();
        service = parseTarget(value);
        return this;
    }

    /**
     * Builds the wire.
     *
     * @return the built wire
     */
    public WireDefinition build() {
        checkState();
        freeze();
        return new WireDefinition(reference, service, true);
    }

    protected WireDefinitionBuilder() {
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
