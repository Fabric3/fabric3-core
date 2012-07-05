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
package org.fabric3.introspection.xml.common;

import java.util.List;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
 */
public class BindingHelper {

    /**
     * Configures the default binding name if no name is specified in a composite.
     *
     * @param binding  the binding
     * @param bindings the existing configured bindings parsed prior to this one
     * @param reader   the stream reader
     * @param context  the introspection context
     */
    public static void configureName(BindingDefinition binding,
                                     List<BindingDefinition> bindings,
                                     XMLStreamReader reader,
                                     IntrospectionContext context) {
        String name = binding.getType().getLocalPart();
        if (searchName(name, bindings)) {
            binding.setName(name);
            BindingNameNotConfigured error = new BindingNameNotConfigured(binding.getType().toString(), reader);
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
     * @param reader   the stream reader
     * @param context  the introspection context
     * @return true if the bindings do not contain duplicates, otherwise false
     */
    public static boolean checkDuplicateNames(BindingDefinition binding,
                                              List<BindingDefinition> bindings,
                                              XMLStreamReader reader,
                                              IntrospectionContext context) {
        for (BindingDefinition definition : bindings) {
            String bindingName = definition.getName();
            if (bindingName.equals(binding.getName())) {
                InvalidBindingName error = new InvalidBindingName("Duplicate binding named " + bindingName, reader);
                context.addError(error);
                return false;
            }
        }
        return true;
    }

    private static boolean searchName(String name, List<BindingDefinition> bindings) {
        for (BindingDefinition entry : bindings) {
            if (name.equals(entry.getName())) {
                return true;
            }
        }
        return false;
    }


}
