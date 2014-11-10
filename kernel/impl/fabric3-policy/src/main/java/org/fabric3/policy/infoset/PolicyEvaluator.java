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
package org.fabric3.policy.infoset;

import java.util.Collection;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalScaArtifact;

/**
 * Evaluates an XPath policy expression against the logical domain model.
 */
public interface PolicyEvaluator {

    /**
     * Evaluates the XPath expression against the the target component, i.e. selects it or one of its children.
     *
     * @param xpathExpression the XPath expression
     * @param target          the target component
     * @return a list of selected nodes, i.e. LogicalComponent, LogicalService, LogicalReference, LogicalBinding, or LogicalOperation
     * @throws PolicyEvaluationException if there is an exception evaluating the expression
     */
    Collection<LogicalScaArtifact<?>> evaluate(String xpathExpression, LogicalComponent<?> target) throws PolicyEvaluationException;

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
