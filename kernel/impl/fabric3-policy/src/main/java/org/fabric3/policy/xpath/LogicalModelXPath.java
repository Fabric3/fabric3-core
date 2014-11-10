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
package org.fabric3.policy.xpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jaxen.BaseXPath;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.JaxenException;
import org.jaxen.SimpleFunctionContext;
import org.jaxen.SimpleNamespaceContext;
import org.oasisopen.sca.Constants;

import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * An XPath implementation based on Jaxen that traverses the domain logical model and matches XPath expressions against it.
 */
public class LogicalModelXPath extends BaseXPath {
    private static final long serialVersionUID = 7175741342820843731L;

    private static final Function INTENT_REFS = new IntentRefsFunction();
    private static final Function URI_REF = new UriRefFunction();
    private static final Function OPERATION_REF = new OperationRefFunction();

    /**
     * Constructor.
     *
     * @param xpathExpr The XPath expression to evaluate against the domain logical model
     * @throws JaxenException if there is a syntax error while parsing the expression
     */
    public LogicalModelXPath(String xpathExpr) throws JaxenException {
        super(xpathExpr, LogicalModelNavigator.getInstance());
        validateInfoset(xpathExpr);
        // setup namespaces and functions
        SimpleNamespaceContext nc = new SimpleNamespaceContext();
        nc.addNamespace("sca", Constants.SCA_NS);
        nc.addNamespace("f3", org.fabric3.api.Namespaces.F3);
        setNamespaceContext(nc);

        SimpleFunctionContext fc = initFunctionContext();
        setFunctionContext(fc);
    }

    private void validateInfoset(String xpathExpr) throws JaxenException {
        if (xpathExpr.contains(":property[")) {
            throw new JaxenException("Properties are not valid attach to targets: " + xpathExpr);
        }

    }

    public Object evaluate(Object node) throws JaxenException {
        Object result = super.evaluate(node);
        if (result instanceof LogicalComponent) {
            return result;
        } else if (result instanceof Collection) {
            List<Object> newList = new ArrayList<>();
            for (Object member : ((Collection) result)) {
                newList.add(member);
            }
            return newList;
        }
        return result;
    }

    protected Context getContext(Object node) {
        if (node instanceof Context) {
            return (Context) node;
        }
        if (node instanceof LogicalComponent) {
            return super.getContext(node);
        }

        if (node instanceof List) {
            List<Object> newList = new ArrayList<>();

            for (Object o : ((List) node)) {
                newList.add(o);
            }

            return super.getContext(newList);
        }
        return super.getContext(node);
    }

    private SimpleFunctionContext initFunctionContext() {
        // register functions using the SCA and default namespaces
        SimpleFunctionContext fc = new SimpleFunctionContext();
        fc.registerFunction(Constants.SCA_NS, "URIRef", URI_REF);
        fc.registerFunction(null, "URIRef", URI_REF);
        fc.registerFunction(Constants.SCA_NS, "IntentRefs", INTENT_REFS);
        fc.registerFunction(null, "IntentRefs", INTENT_REFS);
        fc.registerFunction(Constants.SCA_NS, "OperationRef", OPERATION_REF);
        fc.registerFunction(null, "OperationRef", OPERATION_REF);
        return fc;
    }

}