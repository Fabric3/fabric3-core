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
package org.fabric3.introspection.xml.common;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class BindingHelperTestCase extends TestCase {
    public static final QName QNAME = new QName("foo", "bar");

    private Location location;

    @SuppressWarnings({"serial"})
    public void testBinding() throws Exception {
        BindingDefinition existingBinding = new BindingDefinition("someBinding", URI.create("target"), QNAME) {
        };
        List<BindingDefinition> bindings = new ArrayList<BindingDefinition>();
        bindings.add(existingBinding);

        BindingDefinition newBinding = new BindingDefinition(URI.create("endpoint"), QNAME) {
        };

        IntrospectionContext context = new DefaultIntrospectionContext();

        BindingHelper.configureName(newBinding, bindings, location, context);

        assertTrue(context.getErrors().isEmpty());
    }

    /**
     * Tests that two bindings configured with the default name.
     *
     * @throws Exception if the test fails
     */
    @SuppressWarnings({"serial"})
    public void testBindingError() throws Exception {
        BindingDefinition existingBinding1 = new BindingDefinition("name", URI.create("target"), QNAME) {
        };

        BindingDefinition existingBinding2 = new BindingDefinition(QNAME.getLocalPart(), URI.create("target"), QNAME) {
        };

        List<BindingDefinition> bindings = new ArrayList<BindingDefinition>();
        bindings.add(existingBinding1);
        bindings.add(existingBinding2);

        BindingDefinition newBinding = new BindingDefinition(URI.create("endpoint"), QNAME) {
        };

        IntrospectionContext context = new DefaultIntrospectionContext();

        BindingHelper.configureName(newBinding, bindings, location, context);

        assertTrue(context.getErrors().get(0) instanceof BindingNameNotConfigured);
    }

    @SuppressWarnings({"serial"})
    public void testSyntheticBindingName() throws Exception {
        BindingDefinition existingBinding = new BindingDefinition("service", URI.create("target"), QNAME) {
        };
        List<BindingDefinition> bindings = new ArrayList<BindingDefinition>();
        bindings.add(existingBinding);

        BindingDefinition newBinding = new BindingDefinition(URI.create("endpoint"), QNAME) {
        };

        IntrospectionContext context = new DefaultIntrospectionContext();

        BindingHelper.configureName(newBinding, bindings, location, context);

        assertTrue(context.getErrors().isEmpty());
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        location = EasyMock.createNiceMock(Location.class);
        EasyMock.replay(location);
    }
}
