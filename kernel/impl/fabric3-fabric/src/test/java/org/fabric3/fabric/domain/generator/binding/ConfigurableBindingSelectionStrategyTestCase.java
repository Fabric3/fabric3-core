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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.domain.generator.binding.BindingProvider;

/**
 *
 */
public class ConfigurableBindingSelectionStrategyTestCase extends TestCase {

    public void testSelectionOrder() throws Exception {
        ConfigurableBindingSelectionStrategy strategy = new ConfigurableBindingSelectionStrategy();
        List<QName> order = new ArrayList<>();
        QName bar = new QName("foo", "bar");
        order.add(bar);
        QName baz = new QName("foo", "baz");
        order.add(baz);
        strategy.setScaBindingOrder(order);

        BindingProvider bazProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(bazProvider.getType()).andReturn(baz);
        EasyMock.replay(bazProvider);
        BindingProvider barProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(barProvider.getType()).andReturn(bar);
        EasyMock.replay(barProvider);

        List<BindingProvider> providers = new ArrayList<>();
        providers.add(bazProvider);
        providers.add(barProvider);
        strategy.order(providers);
        assertEquals(barProvider, providers.get(0));
        assertEquals(bazProvider, providers.get(1));

    }

    public void testNoConfiguredOrderSelection() throws Exception {
        ConfigurableBindingSelectionStrategy strategy = new ConfigurableBindingSelectionStrategy();
        QName bar = new QName("foo", "bar");
        QName baz = new QName("foo", "baz");

        BindingProvider bazProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(bazProvider.getType()).andReturn(baz);
        EasyMock.replay(bazProvider);
        BindingProvider barProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(barProvider.getType()).andReturn(bar);
        EasyMock.replay(barProvider);

        List<BindingProvider> providers = new ArrayList<>();
        providers.add(bazProvider);
        providers.add(barProvider);
        strategy.order(providers);
        assertEquals(bazProvider, providers.get(0));
        assertEquals(barProvider, providers.get(1));
    }

    public void testBadConfigurationSelectionOrder() throws Exception {
        ConfigurableBindingSelectionStrategy strategy = new ConfigurableBindingSelectionStrategy();
        List<QName> order = new ArrayList<>();
        QName nonExistent = new QName("foo", "nonExistent");
        order.add(nonExistent);
        QName bar = new QName("foo", "bar");
        order.add(bar);
        strategy.setScaBindingOrder(order);

        QName baz = new QName("foo", "baz");

        List<BindingProvider> providers = new ArrayList<>();
        BindingProvider bazProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(bazProvider.getType()).andReturn(baz);
        EasyMock.replay(bazProvider);
        BindingProvider barProvider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(barProvider.getType()).andReturn(bar);
        EasyMock.replay(barProvider);
        providers.add(bazProvider);
        providers.add(barProvider);

        strategy.order(providers);
        assertEquals(bazProvider, providers.get(0));
        assertEquals(barProvider, providers.get(1));
    }

}
