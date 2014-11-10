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

        Set<LogicalScaArtifact> artifacts = new HashSet<>();

        List<LogicalComponent<?>> nodeSet = context.getNodeSet();
        for (LogicalComponent<?> component : nodeSet) {
            containsIntents(intentArgs, component, artifacts);
        }
        // Jaxen requires a List, not Set
        return new ArrayList<>(artifacts);
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
        List<String> intentArgs = new ArrayList<>();
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
