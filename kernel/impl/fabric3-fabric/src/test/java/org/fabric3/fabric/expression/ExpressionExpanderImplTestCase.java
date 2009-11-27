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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.expression.ExpressionEvaluator;
import org.fabric3.spi.expression.ExpressionExpansionException;

/**
 * @version $Rev$ $Date$
 */
public class ExpressionExpanderImplTestCase extends TestCase {

    public void testBeginExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn("expression1");
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "${expr1} this is a test";
        String result = expander.expand(expression);
        assertEquals("expression1 this is a test", result);
        EasyMock.verify(evaluator);
    }

    public void testEndExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn("expression1");
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "this is a ${expr1}";
        String result = expander.expand(expression);
        assertEquals("this is a expression1", result);
        EasyMock.verify(evaluator);
    }

    public void testBeginEndExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn("expression1");
        EasyMock.expect(evaluator.evaluate("expr2")).andReturn("expression2");
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "${expr1} this is a ${expr2}";
        String result = expander.expand(expression);
        assertEquals("expression1 this is a expression2", result);
        EasyMock.verify(evaluator);
    }

    public void testMultipleExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn("expression1");
        EasyMock.expect(evaluator.evaluate("expr2")).andReturn("expression2");
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "this ${expr1} is a ${expr2} string";
        String result = expander.expand(expression);
        assertEquals("this expression1 is a expression2 string", result);
        EasyMock.verify(evaluator);
    }

    public void testInvalidExpansion() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "this is a bad ${expr1";
        try {
            expander.expand(expression);
            fail("Invalid expression not caught");
        } catch (ExpressionExpansionException e) {
            // expected
        }
        EasyMock.verify(evaluator);
    }

    public void testNoExpression() throws Exception {
        ExpressionEvaluator evaluator = EasyMock.createMock(ExpressionEvaluator.class);
        EasyMock.expect(evaluator.evaluate("expr1")).andReturn(null);
        EasyMock.replay(evaluator);
        Map<Integer, ExpressionEvaluator> evaluators = new HashMap<Integer, ExpressionEvaluator>();
        evaluators.put(0, evaluator);
        ExpressionExpanderImpl expander = new ExpressionExpanderImpl();
        expander.setEvaluators(evaluators);
        String expression = "this is a ${expr1}";
        try {
            expander.expand(expression);
            fail("ValueNotFoundException for expression not caught");
        } catch (ValueNotFoundException e) {
            // expected
        }
        EasyMock.verify(evaluator);
    }

}