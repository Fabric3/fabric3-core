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
package org.fabric3.fabric.domain.generator.binding;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.annotation.Source;
import org.fabric3.spi.domain.generator.binding.BindingProvider;
import org.fabric3.spi.domain.generator.binding.BindingSelectionStrategy;
import org.oasisopen.sca.annotation.Property;

/**
 * A BindingSelectionStrategy that makes a selection based on an ordered list of bindings. This list is provided via a property which can be sourced
 * from a runtime domain level property specified in systemConfig.xml.
 */
public class ConfigurableBindingSelectionStrategy implements BindingSelectionStrategy {
    private Map<QName, Integer> bindingOrder;
    private BindingProviderComparator comparator = new BindingProviderComparator();

    @Property(required = false)
    @Source("$systemConfig//f3:bindings/f3:binding.sca/f3:provider.order")
    public void setScaBindingOrder(List<QName> order) {
        this.bindingOrder = new HashMap<>(order.size());
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
