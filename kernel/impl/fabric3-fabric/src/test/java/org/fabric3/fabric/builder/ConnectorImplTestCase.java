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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.builder;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorImplTestCase extends TestCase {
    private ConnectorImpl connector;
    private PhysicalWireDefinition definition;
    private PhysicalOperationDefinition operation;
    private PhysicalOperationDefinition callback;
    private Map<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?>> builders;

    public void testCreateWire() throws Exception {
        Wire wire = connector.createWire(definition);
        assertEquals(2, wire.getInvocationChains().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testDispatchToBuilder() throws Exception {
        InterceptorBuilder builder = EasyMock.createMock(InterceptorBuilder.class);
        EasyMock.expect(builder.build(EasyMock.isA(PhysicalInterceptorDefinition.class))).andReturn(null).times(2);
        EasyMock.replay(builder);
        builders.put(PhysicalInterceptorDefinition.class, builder);

        PhysicalInterceptorDefinition interceptorDefinition = new PhysicalInterceptorDefinition();
        operation.addInterceptor(interceptorDefinition);
        callback.addInterceptor(interceptorDefinition);

        connector.createWire(definition);
        EasyMock.verify(builder);
    }

    protected void setUp() throws Exception {
        super.setUp();
        connector = new ConnectorImpl();
        builders = new HashMap<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?>>();
        connector.setInterceptorBuilders(builders);

        PhysicalSourceDefinition sourceDefinition = new PhysicalSourceDefinition();
        sourceDefinition.setUri(URI.create("source"));
        PhysicalTargetDefinition targetDefinition = new PhysicalTargetDefinition();
        targetDefinition.setUri(URI.create("target"));
        Set<PhysicalOperationDefinition> operations = new HashSet<PhysicalOperationDefinition>();
        definition = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        operation = new PhysicalOperationDefinition();
        operation.setName("operation");
        definition.addOperation(operation);
        callback = new PhysicalOperationDefinition();
        callback.setName("callback");
        callback.setCallback(true);
        definition.addOperation(callback);
    }
}
