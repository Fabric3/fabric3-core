/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.expression;

/**
 * Expands strings containing expressions delimited by '${' and '}' by delegating to an ExpressionEvaluator. Expression values may be sourced using a
 * variety of methods, including system and environment properties. Expression expansion is used to subsitute configuration values specified in a
 * composite file with runtime values. For example:
 * <pre>
 * <binding.ws uri="http://${myservice.endpoint}/>
 * </pre>
 *
 * @version $Rev$ $Date$
 */
public interface ExpressionExpander {

    /**
     * A string value containing an expression or expressions to expand. The string may contain multiple expressions.
     *
     * @param value the value to expand
     * @return the expanded string
     * @throws ExpressionExpansionException if an error occurs evaluating an expression
     */
    String expand(String value) throws ExpressionExpansionException;

}
