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
 */
package org.fabric3.introspection.xml.common;

import javax.xml.stream.Location;
import java.util.List;

import org.fabric3.api.model.type.component.Binding;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class BindingHelper {

    /**
     * Configures the default binding name if no name is specified in a composite.
     *
     * @param binding  the binding
     * @param bindings the existing configured bindings parsed prior to this one
     * @param location the location of the binding configuration
     * @param context  the introspection context
     */
    public static void configureName(Binding binding, List<Binding> bindings, Location location, IntrospectionContext context) {
        String name = binding.getType();
        if (searchName(name, bindings)) {
            binding.setName(name);
            BindingNameNotConfigured error = new BindingNameNotConfigured(binding, location);
            context.addError(error);
        } else {
            binding.setName(name);
        }
    }

    /**
     * Checks for duplicate binding names
     *
     * @param binding  the binding to check
     * @param bindings the existing bindings
     * @param location the location of the binding configuration
     * @param context  the introspection context
     * @return true if the bindings do not contain duplicates, otherwise false
     */
    public static boolean checkDuplicateNames(Binding binding, List<Binding> bindings, Location location, IntrospectionContext context) {
        for (Binding definition : bindings) {
            String bindingName = definition.getName();
            if (bindingName.equals(binding.getName())) {
                InvalidBindingName error = new InvalidBindingName("Duplicate binding named " + bindingName, location, definition);
                context.addError(error);
                return false;
            }
        }
        return true;
    }

    private static boolean searchName(String name, List<Binding> bindings) {
        for (Binding entry : bindings) {
            if (name.equals(entry.getName())) {
                return true;
            }
        }
        return false;
    }

}
