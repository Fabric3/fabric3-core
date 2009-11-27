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
package org.fabric3.implementation.rs.introspection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import org.fabric3.implementation.java.model.JavaImplementation;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.implementation.rs.model.RsBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;

/**
 * @version $Rev$ $Date$
 */
public class RsHeuristicImplTestCase extends TestCase {

    private IntrospectionContext context;
    private RsHeuristicImpl heuristic;
    private JavaImplementation impl;
    private IMocksControl control;
    private IntrospectionHelper helper;

    public void testInvalidResource() throws Exception {

        impl.setImplementationClass(Invalid.class.getName());
        EasyMock.expect(helper.loadClass(impl.getImplementationClass(),
                                         getClass().getClassLoader())).andStubReturn(getClass().getClassLoader().loadClass(impl.getImplementationClass()));
        context.addError(EasyMock.isA(InvalidRsClass.class));
        EasyMock.replay(context);
        EasyMock.replay(helper);
        control.replay();
        heuristic.applyHeuristics(impl, new URI("/test"), context);

        control.verify();
    }

    public void testValidResource() throws Exception {

        impl.setImplementationClass(RsResource.class.getName());
        EasyMock.expect(helper.loadClass(impl.getImplementationClass(),
                                         getClass().getClassLoader())).andStubReturn(getClass().getClassLoader().loadClass(impl.getImplementationClass()));
        EasyMock.replay(context);
        EasyMock.replay(helper);
        control.replay();
        URI uri = new URI("/test");
        heuristic.applyHeuristics(impl, uri, context);


        InjectingComponentType componentType = impl.getComponentType();
        assertNotNull(componentType);
        assertEquals(componentType.getServices().size(), 1);
        ServiceDefinition service = componentType.getServices().get("REST");
        assertNotNull(service);
        List bindings = service.getBindings();
        assertEquals(1, bindings.size());
        RsBindingDefinition def = (RsBindingDefinition) bindings.get(0);
        assertTrue(def.isResource());
        assertEquals(uri, def.getTargetUri());
        ServiceContract contract = service.getServiceContract();
        assertNotNull(contract);
        assertEquals(contract.getOperations().size(), 9);

        control.verify();
    }

    public void testValidProvider() throws Exception {

        impl.setImplementationClass(EntityProvider.class.getName());
        EasyMock.expect(helper.loadClass(impl.getImplementationClass(),
                                         getClass().getClassLoader())).andStubReturn(getClass().getClassLoader().loadClass(impl.getImplementationClass()));
        EasyMock.replay(context);
        EasyMock.replay(helper);
        control.replay();
        URI uri = new URI("/test");
        heuristic.applyHeuristics(impl, uri, context);


        InjectingComponentType componentType = impl.getComponentType();
        assertNotNull(componentType);
        assertEquals(componentType.getServices().size(), 1);
        ServiceDefinition service = componentType.getServices().get("REST");
        assertNotNull(service);
        List bindings = service.getBindings();
        assertEquals(1, bindings.size());
        RsBindingDefinition def = (RsBindingDefinition) bindings.get(0);
        assertTrue(def.isProvider());
        assertEquals(uri, def.getTargetUri());
        ServiceContract contract = service.getServiceContract();
        assertNotNull(contract);
        control.verify();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getClassLoader()).andStubReturn(getClass().getClassLoader());
        impl = new JavaImplementation();
        impl.setComponentType(new InjectingComponentType());
        control = EasyMock.createControl();
        helper = EasyMock.createNiceMock(IntrospectionHelper.class);


        this.heuristic = new RsHeuristicImpl(helper);

    }

    public static class Invalid {
    }

    public static class Entity implements Serializable {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Path("/Hello")
    public static class RsResource {

        @POST
        @Produces("text/plain")
        @Consumes("application/x-www-form-urlencoded")
        public String resource1(String name) {
            return name;
        }

        @POST
        @Produces("text/plain")
        @Consumes("application/x-www-form-urlencoded")
        public String resource2(String name) {
            return name;
        }

        @POST
        @Produces("text/plain")
        @Consumes("application/x-www-form-urlencoded")
        public Entity resource2(String name1, Entity name2) {
            return name2;
        }

        @POST
        @Path("resource3_1")
        @Consumes("application/x-www-form-urlencoded")
        public String resource3(String name) {
            return name;
        }

        @POST
        @Path("resource3_2")
        public Entity resource3(Entity name) {
            return name;
        }

        @POST
        @Produces("text/plain")
        public String resource4(String name) {
            return name;
        }

        @POST
        @Produces("application/entity")
        public Entity resource4(Entity name) {
            return name;
        }

        @POST
        @Consumes("application/x-www-form-urlencoded")
        public String resource5(String name) {
            return name;
        }

        @POST
        @Consumes("application/entity")
        public Entity resource5(Entity name) {
            return name;
        }
    }

    @Provider
    @Produces("application/entity")
    @Consumes("application/entity")
    public static class EntityProvider implements MessageBodyReader<Entity>, MessageBodyWriter<Entity> {

        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            throw new UnsupportedOperationException();
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
                                   MediaType mediaType) {
            throw new UnsupportedOperationException();
        }

        public long getSize(Entity entity, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            throw new UnsupportedOperationException();
        }

        public void writeTo(Entity data,
                            Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType, MultivaluedMap<String, Object> headers,
                            OutputStream entityStream) throws IOException {
            throw new UnsupportedOperationException();
        }

        public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2) {
            throw new UnsupportedOperationException();
        }

        public long getSize(Entity arg0) {
            throw new UnsupportedOperationException();
        }

        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2) {
            throw new UnsupportedOperationException();
        }

        public Entity readFrom(Class<Entity> arg0,
                               Type arg1,
                               Annotation[] arg2,
                               MediaType arg3,
                               MultivaluedMap<String, String> arg4,
                               InputStream arg5) throws IOException, WebApplicationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
