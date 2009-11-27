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
package org.fabric3.implementation.pojo.reflection;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;

import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitializationException;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveInstanceWrapperTestCase extends TestCase {
    private ReflectiveInstanceWrapper<Object> wrapper;
    private Object instance;
    private EventInvoker<Object> initInvoker;
    private EventInvoker<Object> destroyInvoker;
    private ClassLoader cl;

    public void testWithNoCallbacks() {
        wrapper = new ReflectiveInstanceWrapper<Object>(instance, false, cl, null, null, null, null);
        WorkContext workContext = new WorkContext();
        try {
            wrapper.start(workContext);
        } catch (InstanceInitializationException e) {
            fail();
        }
        try {
            wrapper.stop(workContext);
        } catch (InstanceDestructionException e) {
            fail();
        }
    }

    public void testWithStartCallback() throws ObjectCallbackException {
        initInvoker.invokeEvent(instance);
        EasyMock.replay(initInvoker);
        wrapper = new ReflectiveInstanceWrapper<Object>(instance, false, cl, initInvoker, null, null, null);
        try {
            WorkContext workContext = new WorkContext();
            wrapper.start(workContext);
        } catch (InstanceInitializationException e) {
            fail();
        }
        EasyMock.verify(initInvoker);
    }

    public void testWithStopCallback() throws ObjectCallbackException {
        destroyInvoker.invokeEvent(instance);
        EasyMock.replay(destroyInvoker);
        wrapper = new ReflectiveInstanceWrapper<Object>(instance, false, cl, null, destroyInvoker, null, null);
        try {
            WorkContext workContext = new WorkContext();
            wrapper.start(workContext);
            wrapper.stop(workContext);
        } catch (InstanceDestructionException e) {
            fail();
        } catch (InstanceInitializationException e) {
            fail();
        }
        EasyMock.verify(destroyInvoker);
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        cl = getClass().getClassLoader();
        instance = new Object();
        initInvoker = createMock(EventInvoker.class);
        destroyInvoker = createMock(EventInvoker.class);
    }
}
