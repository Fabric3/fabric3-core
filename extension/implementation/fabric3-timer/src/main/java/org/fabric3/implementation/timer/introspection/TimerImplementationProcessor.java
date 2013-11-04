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
package org.fabric3.implementation.timer.introspection;

import org.fabric3.api.implementation.timer.annotation.Timer;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.implementation.timer.model.TimerImplementation;
import org.fabric3.api.implementation.timer.model.TimerType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.introspection.AbstractPojoImplementationProcessor;
import org.fabric3.implementation.java.introspection.JavaImplementationIntrospector;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class TimerImplementationProcessor extends AbstractPojoImplementationProcessor {
    public TimerImplementationProcessor(@Reference JavaContractProcessor processor,
                                        @Reference JavaImplementationIntrospector introspector,
                                        @Reference IntrospectionHelper helper) {
        super(processor, introspector, helper);
    }

    protected JavaImplementation createImplementation(Class<?> clazz, IntrospectionContext context) {
        TimerImplementation implementation = new TimerImplementation();
        implementation.setImplementationClass(clazz.getName());
        InjectingComponentType componentType = new InjectingComponentType(clazz.getName());
        implementation.setComponentType(componentType);

        TimerData data = new TimerData();
        Timer annotation = clazz.getAnnotation(Timer.class);

        data.setInitialDelay(annotation.initialDelay());

        data.setPoolName(annotation.pool());

        TimerType type = annotation.type();
        data.setType(type);
        switch (type) {

            case ONCE:
                long fireOnce = annotation.fireOnce();
                if (fireOnce == TimerData.UNSPECIFIED) {
                    InvalidAnnotation error = new InvalidAnnotation("The fireOnce attribute must be specified for the timer", clazz, annotation, clazz);
                    context.addError(error);
                }
                data.setFireOnce(fireOnce);
                break;
            case FIXED_RATE:
                long fixedRate = annotation.fixedRate();
                if (fixedRate == TimerData.UNSPECIFIED) {
                    InvalidAnnotation error = new InvalidAnnotation("The fixedRate attribute must be specified for the timer", clazz, annotation, clazz);
                    context.addError(error);
                }
                data.setFixedRate(fixedRate);
                break;
            case INTERVAL:
                long repeatInterval = annotation.repeatInterval();
                if (repeatInterval == TimerData.UNSPECIFIED) {
                    InvalidAnnotation error = new InvalidAnnotation("The repeatInterval attribute must be specified for the timer", clazz, annotation, clazz);
                    context.addError(error);
                }
                data.setRepeatInterval(repeatInterval);
                break;
            case RECURRING:
                if (!isInterval(clazz)) {
                    InvalidAnnotation error = new InvalidAnnotation("Timer class must implement an interval method", clazz, annotation, clazz);
                    context.addError(error);
                } else {
                    data.setIntervalMethod(true);
                }
                break;
        }
        implementation.setTimerData(data);
        return implementation;
    }

    private boolean isInterval(Class<?> clazz) {
        try {
            clazz.getMethod("nextInterval");
            return true;
        } catch (NoSuchMethodException e) {
            // ignore
        }
        return false;
    }

}
