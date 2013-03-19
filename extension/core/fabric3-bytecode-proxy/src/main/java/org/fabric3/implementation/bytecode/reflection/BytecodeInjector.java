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

import org.fabric3.implementation.pojo.objectfactory.MultiplicityObjectFactory;
import org.fabric3.spi.objectfactory.InjectionAttributes;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 *
 */
public abstract class BytecodeInjector implements Injector<Object> {
    private ObjectFactory<?> objectFactory;

    public void init(ObjectFactory<?> objectFactory) {
        this.objectFactory = objectFactory;
    }

    /**
     * Inject a new value on the given instance
     */
    public void inject(Object instance) throws ObjectCreationException {
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
        inject(instance, target);
        // TODO inject the target
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

    protected abstract void inject(Object instance, Object target);


}
