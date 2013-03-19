/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 */
package org.fabric3.implementation.bytecode.reflection;

import java.lang.reflect.Constructor;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import org.fabric3.implementation.pojo.spi.reflection.IncompatibleArgumentException;
import org.fabric3.implementation.pojo.spi.reflection.NullPrimitiveException;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Uses bytecode generation to instantiate an object instance.
 */
public class BytecodeObjectFactory<T> implements ObjectFactory<T> {
    private Constructor<T> constructor;
    private final ConstructorAccess<T> access;
    private final ObjectFactory<?>[] paramFactories;

    /**
     * Constructor.
     *
     * @param constructor    the constructor to instantiate the instance with
     * @param access         the constructor accessor to use for instance instantiation
     * @param paramFactories factories for creating constructor parameters
     */
    public BytecodeObjectFactory(Constructor<T> constructor, ConstructorAccess<T> access, ObjectFactory<?>[] paramFactories) {
        this.constructor = constructor;
        this.access = access;
        this.paramFactories = paramFactories;
    }

    public T getInstance() throws ObjectCreationException {
//        try {
            if (paramFactories == null) {
                return access.newInstance();
            } else {
                Object[] params = new Object[paramFactories.length];
                for (int i = 0; i < paramFactories.length; i++) {
                    ObjectFactory<?> paramFactory = paramFactories[i];
                    params[i] = paramFactory.getInstance();
                }
                try {
                    return access.newInstance(params);
                } catch (IllegalArgumentException e) {
                    // check which of the parameters could not be assigned
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    String name = constructor.toString();
                    for (int i = 0; i < paramTypes.length; i++) {
                        Class<?> paramType = paramTypes[i];
                        if (paramType.isPrimitive() && params[i] == null) {
                            throw new NullPrimitiveException(name, i);
                        }
                        if (params[i] != null && !paramType.isInstance(params[i])) {
                            throw new IncompatibleArgumentException(name, i, params[i].getClass().getName());
                        }
                    }
                    // did not fail because of incompatible assignment
                    throw new ObjectCreationException(name, e);
                }
            }
//        } catch (IllegalAccessException e) {
//            String id = constructor.toString();
//            throw new AssertionError("Constructor is not accessible: " + id);
//        } catch (InvocationTargetException e) {
//            String id = constructor.toString();
//            throw new ObjectCreationException("Exception thrown by constructor: " + id, id, e.getCause());
//        }
    }
}
