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

import org.fabric3.host.Namespaces;
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
        // setup namespaces and functions
        SimpleNamespaceContext nc = new SimpleNamespaceContext();
        nc.addNamespace("sca", Constants.SCA_NS);
        nc.addNamespace("f3", Namespaces.F3);
        setNamespaceContext(nc);

        SimpleFunctionContext fc = initFunctionContext();
        setFunctionContext(fc);
    }

    public Object evaluate(Object node) throws JaxenException {
        Object result = super.evaluate(node);
        if (result instanceof LogicalComponent) {
            return result;
        } else if (result instanceof Collection) {
            List<Object> newList = new ArrayList<Object>();
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
            List<Object> newList = new ArrayList<Object>();

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