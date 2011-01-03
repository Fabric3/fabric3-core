/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.transform;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.xsd.XSDConstants;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerFactory;

/**
 * @version $Rev$ $Date$
 */
public class DefaultTransformerRegistryTestCase extends TestCase {
    private DefaultTransformerRegistry registry;

    public void testTransformerFactoryRegistration() throws Exception {
        List<TransformerFactory> factories = new ArrayList<TransformerFactory>();
        factories.add(new MockFactory());
        registry.setFactories(factories);
        JavaClass<Integer> target = new JavaClass<Integer>(Integer.class);
        List<Class<?>> targets = new ArrayList<Class<?>>();
        targets.add(Integer.class);
        assertNotNull(registry.getTransformer(XSDConstants.PROPERTY_TYPE, target, targets, targets));
    }

    private class MockFactory implements TransformerFactory {

        public boolean canTransform(DataType<?> source, DataType<?> target) {
            return true;
        }

        public Transformer<Object, Object> create(DataType<?> source, DataType<?> target, List<Class<?>> sourceTypes, List<Class<?>> targetTypes)
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
