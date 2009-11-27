/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.implementation.mock;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;

/**
 * @version $Rev$ $Date$
 */
public class MockComponentTypeLoaderImplTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testLoad() throws Exception {

        IntrospectionContext context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getClassLoader()).andReturn(getClass().getClassLoader());
        EasyMock.replay(context);


        JavaContractProcessor processor = EasyMock.createMock(JavaContractProcessor.class);
        JavaServiceContract controlContract = new JavaServiceContract(IMocksControl.class);
        JavaServiceContract fooContract = new JavaServiceContract(Foo.class);
        EasyMock.expect(processor.introspect(
                EasyMock.eq(IMocksControl.class),
                EasyMock.isA(IntrospectionContext.class))).andReturn(controlContract);
        EasyMock.expect(processor.introspect(
                EasyMock.eq(Foo.class),
                EasyMock.isA(IntrospectionContext.class))).andReturn(fooContract);
        EasyMock.replay(processor);

        MockComponentTypeLoader componentTypeLoader = new MockComponentTypeLoaderImpl(processor);

        List<String> mockedInterfaces = new LinkedList<String>();
        mockedInterfaces.add(Foo.class.getName());

        MockComponentType componentType = componentTypeLoader.load(mockedInterfaces, context);

        assertNotNull(componentType);
        java.util.Map<String, ServiceDefinition> services = componentType.getServices();

        assertEquals(2, services.size());    // 4 because the mock service is added implicitly

        ServiceDefinition service = services.get("Foo");
        assertNotNull(service);
        assertEquals(Foo.class.getName(), service.getServiceContract().getQualifiedInterfaceName());


    }

}
