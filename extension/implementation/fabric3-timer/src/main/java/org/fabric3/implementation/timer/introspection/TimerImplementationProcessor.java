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
 */
package org.fabric3.implementation.timer.introspection;

import org.fabric3.api.annotation.wire.Key;
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
@Key("timer")
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
