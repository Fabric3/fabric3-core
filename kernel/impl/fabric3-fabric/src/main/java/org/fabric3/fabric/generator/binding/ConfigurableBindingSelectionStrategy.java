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
package org.fabric3.fabric.generator.binding;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Property;

import org.fabric3.spi.binding.provider.BindingProvider;
import org.fabric3.spi.binding.provider.BindingSelectionStrategy;

/**
 * A BindingSelectionStrategy that makes a selection based on an ordered list of bindings. This list is provided via a property which can be sourced
 * from a runtime domain level property specified in systemConfig.xml.
 */
public class ConfigurableBindingSelectionStrategy implements BindingSelectionStrategy {
    private Map<QName, Integer> bindingOrder;
    private BindingProviderComparator comparator = new BindingProviderComparator();

    @Property(required = false)
    public void setScaBindingOrder(List<QName> order) {
        this.bindingOrder = new HashMap<QName, Integer>(order.size());
        for (int i = 0; i < order.size(); i++) {
            QName name = order.get(i);
            bindingOrder.put(name, i);
        }
    }

    public void order(List<BindingProvider> providers) {
        if (bindingOrder == null || providers.isEmpty()) {
            return;
        }
        Collections.sort(providers, comparator);
    }


    private class BindingProviderComparator implements Comparator<BindingProvider> {
        public int compare(BindingProvider one, BindingProvider two) {
            Integer posOne = bindingOrder.get(one.getType());
            if (posOne == null) {
                posOne = -1;
            }
            Integer posTwo = bindingOrder.get(two.getType());
            if (posTwo == null) {
                posTwo = -1;
            }
            return posOne - posTwo;
        }
    }
}
