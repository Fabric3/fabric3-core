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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.reflection;

import java.lang.reflect.Field;

import org.fabric3.implementation.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.spi.objectfactory.InjectionAttributes;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Injects a value created by an {@link ObjectFactory} on a given field.
 */
public class FieldInjector implements Injector<Object> {
    private final Field field;

    private ObjectFactory<?> objectFactory;

    /**
     * Create an injector and have it use the given <code>ObjectFactory</code> to inject a value on the instance using the reflected
     * <code>Field</code>
     *
     * @param field         target field to inject on
     * @param objectFactory the object factor that creates the value to inject
     */
    public FieldInjector(Field field, ObjectFactory<?> objectFactory) {
        this.field = field;
        this.field.setAccessible(true);
        this.objectFactory = objectFactory;
    }

    /**
     * Inject a new value on the given instance
     */
    public void inject(Object instance) throws ObjectCreationException {
        try {
            Object target;
            if (objectFactory == null) {
                // this can happen if a value is removed such as a reference being un-wired
                target = null;
            } else {
                target = objectFactory.getInstance();
                if (target == null) {
                    // The object factory is "empty", e.g. a reference has not been wired yet. Avoid injecting onto the instance.
                    // Note this is a correct assumption as there is no mechanism for configuring null values in SCA
                    return;
                }
            }            
            field.set(instance, target);
        } catch (IllegalAccessException e) {
            String id = field.getName();
            throw new AssertionError("Field is not accessible:" + id);
        }
    }

    public void setObjectFactory(ObjectFactory<?> objectFactory, InjectionAttributes attributes) {

        if (this.objectFactory instanceof MultiplicityObjectFactory<?>) {
            ((MultiplicityObjectFactory<?>) this.objectFactory).addObjectFactory(objectFactory, attributes);
        } else {
            this.objectFactory = objectFactory;
        }

    }

    public void clearObjectFactory() {
        if (this.objectFactory instanceof MultiplicityObjectFactory<?>) {
            ((MultiplicityObjectFactory<?>) this.objectFactory).clear();
        } else {
            objectFactory = null;
        }
    }
}
