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
package org.fabric3.transform;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.TypeConstants;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerFactory;

/**
 *
 */
public class DefaultTransformerRegistryTestCase extends TestCase {
    private DefaultTransformerRegistry registry;

    public void testTransformerFactoryRegistration() throws Exception {
        List<TransformerFactory> factories = new ArrayList<>();
        factories.add(new MockFactory());
        registry.setFactories(factories);
        JavaType target = new JavaType(Integer.class);
        List<Class<?>> targets = new ArrayList<>();
        targets.add(Integer.class);
        assertNotNull(registry.getTransformer(TypeConstants.PROPERTY_TYPE, target, targets, targets));
    }

    private class MockFactory implements TransformerFactory {

        public int getOrder() {
            return 0;
        }

        public boolean canTransform(DataType source, DataType target) {
            return true;
        }

        public Transformer<Object, Object> create(DataType source, DataType target, List<Class<?>> sourceTypes, List<Class<?>> targetTypes)
                throws TransformationException {
            return new MockTransformer();
        }

    }

    private class MockTransformer implements Transformer<Object, Object> {

        public Object transform(Object o, ClassLoader loader) throws TransformationException {
            return null;
        }

    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = new DefaultTransformerRegistry();
    }

}
