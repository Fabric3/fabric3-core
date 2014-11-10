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
        List<BindingDefinition> bindings = new ArrayList<>();
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

        List<BindingDefinition> bindings = new ArrayList<>();
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
        List<BindingDefinition> bindings = new ArrayList<>();
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
