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
package org.fabric3.fabric.expression;

import java.util.Map;
import java.util.TreeMap;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.expression.ExpressionEvaluator;
import org.fabric3.spi.expression.ExpressionExpander;
import org.fabric3.spi.expression.ExpressionExpansionException;

/**
 * Expands strings containing expressions delimited by '${' and '}' through delegation to a set of ExpressionEvaluators.
 *
 * @version $Rev$ $Date$
 */
public class ExpressionExpanderImpl implements ExpressionExpander {
    private static final String PREFIX = "${";
    private static final String POSTFIX = "}";

    /* evaluators sorted by their configured order */
    private TreeMap<Integer, ExpressionEvaluator> evaluators = new TreeMap<Integer, ExpressionEvaluator>();

    @Reference
    public void setEvaluators(Map<Integer, ExpressionEvaluator> evaluators) {
        this.evaluators.putAll(evaluators);
    }

    public String expand(String value) throws ExpressionExpansionException {
        StringBuilder builder = new StringBuilder();
        expand(value, 0, builder);
        return builder.toString();
    }

    /**
     * Recursively expands expressions contained in the given string starting at the supplied index.e
     *
     * @param value   the string containing the expressions to expand
     * @param index   the starting index
     * @param builder the builder to expand expressions to
     * @return an updated builder with the expanded expressions
     * @throws ExpressionExpansionException if an invalid expression is found  or a value does not exist for the expression, i.e. it is undefined in
     *                                      the ExpressionEvaluator's data set
     */
    private StringBuilder expand(String value, int index, StringBuilder builder) throws ExpressionExpansionException {
        int start = value.indexOf(PREFIX, index);
        if (start == -1) {
            return builder.append(value.substring(index));
        }
        int end = value.indexOf(POSTFIX, index + 2);
        if (end == -1) {
            throw new ExpressionExpansionException("No closing " + POSTFIX + " for expression starting at " + start + " in :" + value);
        }
        if (index != start) {
            builder.append(value.substring(index, start));
        }
        // expand the expression
        String expression = value.substring(start + 2, end);
        String evaluated = evaluate(expression);
        if (evaluated == null) {
            throw new ValueNotFoundException("Value not defined for '" + expression + "' in: " + value);
        }
        builder.append(evaluated);

        if (end < value.length() - 1) {
            expand(value, end + 1, builder);
        }
        return builder;
    }

    /**
     * Iterates through the ExpressionEvaluators until the given expression is evaluated.
     *
     * @param expression the expression to evaluate
     * @return the expanded expression or null if no value can be sourced
     */
    private String evaluate(String expression) {
        for (ExpressionEvaluator evaluator : evaluators.values()) {
            String expanded = evaluator.evaluate(expression);
            if (expanded != null) {
                return expanded;
            }
        }
        return null;
    }


}