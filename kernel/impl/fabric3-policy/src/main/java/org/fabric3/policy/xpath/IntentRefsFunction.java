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
package org.fabric3.policy.xpath;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.model.instance.LogicalService;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

/**
 * Implements the IntentRefs function defined by the SCA Policy Specification.
 */
public class IntentRefsFunction implements Function {

    @SuppressWarnings({"unchecked"})
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.isEmpty()) {
            throw new FunctionCallException("Invalid number of arguments specified in IntentRefs(): " + args.size());
        }
        List<String> intentArgs = normalizeIntents(args);

        Set<LogicalScaArtifact> artifacts = new HashSet<LogicalScaArtifact>();

        List<LogicalComponent<?>> nodeSet = context.getNodeSet();
        for (LogicalComponent<?> component : nodeSet) {
            containsIntents(intentArgs, component, artifacts);
        }
        // Jaxen requires a List, not Set
        return new ArrayList<LogicalScaArtifact>(artifacts);
    }

    private void containsIntents(List<String> intentArgs, LogicalComponent<?> component, Set<LogicalScaArtifact> artifacts) {
        if (containsIntents(intentArgs, component.getDefinition().getImplementation().getIntents())) {
            artifacts.add(component);
        }
        if (containsIntents(intentArgs, component.getDefinition().getIntents())) {
            artifacts.add(component);
        }
        if (containsIntents(intentArgs, component.getIntents())) {
            artifacts.add(component);
        }

        for (LogicalService service : component.getServices()) {
            if (containsIntents(intentArgs, service.getIntents())) {
                artifacts.add(service);
            }
        }

        for (LogicalReference reference : component.getReferences()) {
            if (containsIntents(intentArgs, reference.getIntents())) {
                artifacts.add(reference);
            }
        }

        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> childComponent : composite.getComponents()) {
                containsIntents(intentArgs, childComponent, artifacts);
            }
        }
    }

    private boolean containsIntents(Collection<String> intentArgs, Set<QName> intents) {
        boolean contains = true;
        for (String intentArg : intentArgs) {
            if (!containsIntent(intentArg, intents)) {
                contains = false;
                break;
            }
        }
        return contains;
    }

    private boolean containsIntent(String intentArg, Set<QName> intents) {
        boolean found = false;
        for (QName intent : intents) {
            if (intent.getLocalPart().equals(intentArg)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private List<String> normalizeIntents(List args) throws FunctionCallException {
        if (!(args.get(0) instanceof String)) {
            throw new FunctionCallException("Invalid argument type specified in IntentRefs(): " + args.get(0).getClass());
        }
        List<String> intentArgs = new ArrayList<String>();
        for (Object arg : args) {
            String stringArg = (String) arg;
            int pos = stringArg.indexOf(":");
            if (pos >= 0) {
                intentArgs.add(stringArg.substring(pos + 1));
            }
        }
        return intentArgs;
    }

}
