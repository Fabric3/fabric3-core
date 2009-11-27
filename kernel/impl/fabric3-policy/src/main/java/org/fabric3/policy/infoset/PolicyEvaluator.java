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
package org.fabric3.policy.infoset;

import java.util.List;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalScaArtifact;

/**
 * Evaluates an XPath policy expression against the logical domain model.
 *
 * @version $Rev$ $Date$
 */
public interface PolicyEvaluator {

    /**
     * Evaluates the XPath expression against the the target component, i.e. selects it or one of its children.
     *
     * @param xpathExpression the XPath expression
     * @param target          the target component
     * @return a list of selected nodes, i.e. LogicalComponnet, LogicalService, LogicalReference, LogicalBinding, or LogicalOperation
     * @throws PolicyEvaluationException if there is an exception evaluating the expression
     */
    List<LogicalScaArtifact<?>> evaluate(String xpathExpression, LogicalComponent<?> target) throws PolicyEvaluationException;

    /**
     * Determines if the XPath expression applies to the target component, i.e. selects it or one of its children.
     *
     * @param appliesToXPath the XPath expression
     * @param target         the target logical artifact to resolve against
     * @return true if the expression applies
     * @throws PolicyEvaluationException if there is an exception evaluating the expression
     */
    boolean doesApply(String appliesToXPath, LogicalScaArtifact<?> target) throws PolicyEvaluationException;

    /**
     * Returns true if the given XPath expression attaches to the target starting at the given context.
     *
     * @param attachesToXPath the XPath expression
     * @param target          the target component
     * @param context         the context
     * @return true if the expression attaches
     * @throws PolicyEvaluationException if there is an exception evaluating the expression
     */
    boolean doesAttach(String attachesToXPath, LogicalComponent<?> target, LogicalComponent<?> context) throws PolicyEvaluationException;


}
