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
package org.fabric3.binding.ws.metro.generator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Maps policy expressions to the WSDL operations they are attached to. Note that operation names may not correspond to a Java method name on the
 * service implementation class as the WSDL operation name may be specified using the <code>@WebMethod.operationName()</code> attribute. In this case,
 * the WSDL operation name will correspond to the attribute value and not the method name.
 * <p/>
 * Operation overloading is not supported.
 *
 * @version $Rev$ $Date$
 */
public class PolicyExpressionMapping implements Serializable {
    private static final long serialVersionUID = 5554492976250872672L;

    private String id;
    private Element policyExpression;
    private List<String> operations = new ArrayList<String>();

    /**
     * Constructor.
     *
     * @param id               the policy expression id provided as an attribute on the Policy element as defined by the WS-Security utility
     *                         extensions.
     * @param policyExpression the WS-Policy policy expression rendered as a DOM
     */
    public PolicyExpressionMapping(String id, Element policyExpression) {
        this.id = id;
        this.policyExpression = policyExpression;
    }

    public String getId() {
        return id;
    }

    public Element getPolicyExpression() {
        return policyExpression;
    }

    public void addOperationName(String name) {
        operations.add(name);
    }

    public List<String> getOperationNames() {
        return operations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PolicyExpressionMapping that = (PolicyExpressionMapping) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (policyExpression != null ? policyExpression.hashCode() : 0);
        result = 31 * result + (operations != null ? operations.hashCode() : 0);
        return result;
    }
}
