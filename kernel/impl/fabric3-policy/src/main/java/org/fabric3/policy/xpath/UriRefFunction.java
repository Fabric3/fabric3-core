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

import java.util.Collections;
import java.util.List;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Implements the URIRef function defined by the SCA Policy Specification.
 *
 * @version $Rev$ $Date$
 */
public class UriRefFunction implements Function {

    @SuppressWarnings({"unchecked"})
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() != 1) {
            throw new FunctionCallException("Invalid number of arguments for URIRef(): " + args.size());
        }
        String uri = args.get(0).toString();
        List<LogicalComponent<?>> nodeSet = context.getNodeSet();
        for (LogicalComponent<?> component : nodeSet) {
            if (component.getUri().getSchemeSpecificPart().equals(uri)) {
                return component;
            } else if (component instanceof LogicalCompositeComponent) {
                LogicalComponent ret = find(uri, (LogicalCompositeComponent) component);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Recurses the composite hierarchy for the component with the given URI
     *
     * @param uri       the uri
     * @param composite the composite to recurse
     * @return the component or null if not found
     */
    private LogicalComponent find(String uri, LogicalCompositeComponent composite) {
        for (LogicalComponent child : composite.getComponents()) {
            if (child.getUri().getSchemeSpecificPart().equals(uri)) {
                return child;
            }
            if (child instanceof LogicalCompositeComponent) {
                LogicalComponent component = find(uri, (LogicalCompositeComponent) child);
                if (component != null) {
                    return component;
                }
            }
        }
        return null;

    }
}
