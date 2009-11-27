/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.exist.introspection;

import javax.xml.namespace.QName;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionException;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.xquery.scdl.XQueryComponentType;
import org.fabric3.xquery.scdl.XQueryImplementation;

public class XQueryImplementationProcessorImplTestCase extends TestCase {

    private IntrospectionContext context;
    private XQueryImplementationProcessorImpl processor;
    private XQueryImplementation impl;
    private IMocksControl control;

    public void testValidModule() throws IntrospectionException {
        impl.setLocation("tests/xquery/valid.xqm");

        control.replay();
        processor.introspect(impl, context);


        XQueryComponentType componentType = impl.getComponentType();
        assertNotNull(componentType);
        assertTrue(impl.isIsModule());
        assertEquals(impl.getModuleNameSpace(), new QName("http://fabric3.codehaus.org/xquery/test/module", "echoModule"));

        assertEquals(componentType.getProperties().size(), 1);
        Property prop = componentType.getProperties().get("message");
        assertNotNull(prop);

        assertEquals(componentType.getServices().size(), 1);
        ServiceDefinition service = componentType.getServices().get("EchoService");
        assertNotNull(service);
        ServiceContract contract = service.getServiceContract();
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "hello");

        assertEquals(componentType.getReferences().size(), 1);
        ReferenceDefinition reference = componentType.getReferences().get("java");
        assertNotNull(reference);
        contract = reference.getServiceContract();
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "hello");
        
        control.verify();
    }

    public void testValidQuery() throws IntrospectionException {
        impl.setLocation("tests/xquery/valid.xq");

        control.replay();
        processor.introspect(impl, context);


        XQueryComponentType componentType = impl.getComponentType();
        assertNotNull(componentType);
        assertFalse(impl.isIsModule());

        assertEquals(componentType.getProperties().size(), 1);
        Property prop = componentType.getProperties().get("message");
        assertNotNull(prop);

        assertEquals(componentType.getServices().size(), 2);

        ServiceDefinition service = componentType.getServices().get("EchoService");
        assertNotNull(service);
        ServiceContract contract = service.getServiceContract();
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "hello");

        service = componentType.getServices().get("XQueryService");
        assertNotNull(service);
        contract = service.getServiceContract();
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "evaluate");

        assertEquals(componentType.getReferences().size(), 1);
        ReferenceDefinition reference = componentType.getReferences().get("java");
        assertNotNull(reference);
        contract = reference.getServiceContract();
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "hello");

        control.verify();
    }
    
        public void testCallback() throws IntrospectionException {
        impl.setLocation("tests/xquery/callback.xqm");

        control.replay();
        processor.introspect(impl, context);


        XQueryComponentType componentType = impl.getComponentType();
        assertNotNull(componentType);
        assertTrue(impl.isIsModule());
        assertEquals(impl.getModuleNameSpace(), new QName("http://fabric3.codehaus.org/xquery/test/callback", "callbackModule"));

        assertEquals(componentType.getProperties().size(), 1);
        Property prop = componentType.getProperties().get("message");
        assertNotNull(prop);

        assertEquals(componentType.getServices().size(), 2);
        ServiceDefinition service = componentType.getServices().get("EchoService");
        assertNotNull(service);
        ServiceContract contract = service.getServiceContract();
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "hello");
        contract = contract.getCallbackContract();
        assertNotNull(contract);
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "callback");
        
        service = componentType.getServices().get("JavaCallback");
        assertNotNull(service);
        contract = service.getServiceContract();
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "callback");


        assertEquals(componentType.getReferences().size(), 1);
        ReferenceDefinition reference = componentType.getReferences().get("java");
        assertNotNull(reference);
        contract = reference.getServiceContract();
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "hello");
        contract = contract.getCallbackContract();
        assertNotNull(contract);
        assertEquals(contract.getOperations().size(), 1);
        assertEquals(((Operation) contract.getOperations().get(0)).getName(), "callback");
        
        control.verify();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ClassLoader cl = getClass().getClassLoader();
        impl = new XQueryImplementation();
        IntrospectionHelper helper = EasyMock.createNiceMock(IntrospectionHelper.class);
        EasyMock.replay(helper);


        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTargetClassLoader()).andStubReturn(cl);
        EasyMock.replay(context);

        control = EasyMock.createControl();
        this.processor = new XQueryImplementationProcessorImpl(helper);

    }
}
