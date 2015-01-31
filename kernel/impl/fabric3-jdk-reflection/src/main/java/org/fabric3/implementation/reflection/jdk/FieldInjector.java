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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.reflection.jdk;

import java.lang.reflect.Field;

import org.fabric3.api.host.ContainerException;
import org.fabric3.implementation.pojo.objectfactory.MultiplicityObjectFactory;
import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

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
    public void inject(Object instance) throws ContainerException {
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
