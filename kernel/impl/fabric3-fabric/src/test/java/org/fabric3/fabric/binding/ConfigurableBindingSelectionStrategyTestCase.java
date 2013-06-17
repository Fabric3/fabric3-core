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
package org.fabric3.fabric.binding;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.binding.provider.BindingProvider;

/**
 *
 */
public class ConfigurableBindingSelectionStrategyTestCase extends TestCase {

    public void testSelectionOrder() throws Exception {
        ConfigurableBindingSelectionStrategy strategy = new ConfigurableBindingSelectionStrategy();
        List<QName> order = new ArrayList<QName>();
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

        List<BindingProvider> providers = new ArrayList<BindingProvider>();
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

        List<BindingProvider> providers = new ArrayList<BindingProvider>();
        providers.add(bazProvider);
        providers.add(barProvider);
        strategy.order(providers);
        assertEquals(bazProvider, providers.get(0));
        assertEquals(barProvider, providers.get(1));
    }

    public void testBadConfigurationSelectionOrder() throws Exception {
        ConfigurableBindingSelectionStrategy strategy = new ConfigurableBindingSelectionStrategy();
        List<QName> order = new ArrayList<QName>();
        QName nonExistent = new QName("foo", "nonExistent");
        order.add(nonExistent);
        QName bar = new QName("foo", "bar");
        order.add(bar);
        strategy.setScaBindingOrder(order);

        QName baz = new QName("foo", "baz");

        List<BindingProvider> providers = new ArrayList<BindingProvider>();
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
