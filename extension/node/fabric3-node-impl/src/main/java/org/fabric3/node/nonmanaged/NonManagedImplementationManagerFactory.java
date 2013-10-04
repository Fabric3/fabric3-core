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
package org.fabric3.node.nonmanaged;

import java.lang.reflect.Type;

import org.fabric3.implementation.pojo.manager.ImplementationManager;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.spi.container.component.InstanceDestructionException;
import org.fabric3.spi.container.component.InstanceInitException;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;

/**
 * A factory that enabled non-managed code to be treated as a Java implementation instance.
 */
public class NonManagedImplementationManagerFactory implements ImplementationManagerFactory, ImplementationManager {
    private Object instance;
    private SingletonObjectFactory<?> factory;

    public NonManagedImplementationManagerFactory(Object instance) {
        this.instance = instance;
        factory = new SingletonObjectFactory<Object>(instance);
    }

    public ImplementationManager createManager() {
        return this;
    }

    public Class<?> getImplementationClass() {
        return instance.getClass();
    }

    public void startUpdate() {
    }

    public void endUpdate() {
    }

    public ObjectFactory<?> getObjectFactory(Injectable attribute) {
        return factory;
    }

    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory) {
    }

    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory, InjectionAttributes attributes) {
    }

    public void removeObjectFactory(Injectable injectable) {
    }

    public Class<?> getMemberType(Injectable injectable) {
        throw new UnsupportedOperationException();
    }

    public Type getGenericType(Injectable injectable) {
        throw new UnsupportedOperationException();
    }

    public Object newInstance() throws ObjectCreationException {
        return instance;
    }

    public void start(Object instance) throws InstanceInitException {
    }

    public void stop(Object instance) throws InstanceDestructionException {
    }

    public void reinject(Object instance) throws InstanceLifecycleException {
    }

    public void updated(Object instance, String referenceName) {
    }

    public void removed(Object instance, String referenceName) {
    }
}
