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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.timer.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.fabric3.implementation.java.introspection.ImplementationArtifactNotFound;
import org.fabric3.implementation.java.introspection.JavaImplementationIntrospector;
import org.fabric3.api.implementation.timer.model.TimerImplementation;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.implementation.timer.model.TimerType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.api.implementation.timer.model.TimerData.UNSPECIFIED;

/**
 * Loads <implementation.timer> entries in a composite.
 */
public class TimerImplementationLoader extends AbstractValidatingTypeLoader<TimerImplementation> {
    private final JavaImplementationIntrospector introspector;
    private final LoaderHelper loaderHelper;

    public TimerImplementationLoader(@Reference JavaImplementationIntrospector introspector, @Reference LoaderHelper loaderHelper) {
        this.introspector = introspector;
        this.loaderHelper = loaderHelper;
        addAttributes("class", "intervalClass", "fixedRate", "repeatInterval", "fireOnce", "initialDelay", "unit", "requires", "policySets", "poolName");
    }

    public TimerImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        TimerImplementation implementation = new TimerImplementation();

        validateAttributes(reader, context, implementation);

        if (!processImplementationClass(implementation, reader, context)) {
            // an error with the implementation class, return a dummy implementation
            InjectingComponentType type = new InjectingComponentType(null);
            implementation.setComponentType(type);
            return implementation;
        }
        TimerData data = new TimerData();
        implementation.setTimerData(data);

        String poolName = reader.getAttributeValue(null, "poolName");
        if (poolName != null) {
            data.setPoolName(poolName);
        }
        processInitialDelay(data, reader, startLocation, context);
        processTimeUnit(data, reader, startLocation, context);
        processIntervalClass(reader, context, implementation);
        processRepeatInterval(reader, startLocation, context, implementation);
        processRepeatFixedRate(reader, startLocation, context, implementation);
        processFireOnce(reader, startLocation, context, implementation);
        processIntervalMethod(context, implementation);
        validateData(startLocation, context, data);

        loaderHelper.loadPolicySetsAndIntents(implementation, reader, context);

        String implClass = implementation.getImplementationClass();
        InjectingComponentType componentType = new InjectingComponentType(implClass);
        introspector.introspect(componentType, context);
        implementation.setComponentType(componentType);

        LoaderUtil.skipToEndElement(reader);

        return implementation;
    }

    private void processInitialDelay(TimerData data, XMLStreamReader reader, Location startLocation, IntrospectionContext context) {
        String initialDelay = reader.getAttributeValue(null, "initialDelay");
        if (initialDelay != null) {
            try {
                data.setInitialDelay(Long.parseLong(initialDelay));
            } catch (NumberFormatException e) {
                InvalidValue failure = new InvalidValue("Invalid initial delay", startLocation, e);
                context.addError(failure);
            }
        }
    }

    private void processTimeUnit(TimerData data, XMLStreamReader reader, Location startLocation, IntrospectionContext context) {
        String units = reader.getAttributeValue(null, "unit");
        if (units != null) {
            try {
                TimeUnit timeUnit = TimeUnit.valueOf(units.toUpperCase());
                data.setTimeUnit(timeUnit);
            } catch (IllegalArgumentException e) {
                InvalidValue failure = new InvalidValue("Invalid time unit: " + units, startLocation);
                context.addError(failure);
            }
        }
    }

    private void validateData(Location startLocation, IntrospectionContext context, TimerData data) {
        if (!data.isIntervalMethod() && data.getIntervalClass() == null && data.getFixedRate() == UNSPECIFIED && data.getRepeatInterval() == UNSPECIFIED
            && data.getFireOnce() == UNSPECIFIED) {
            MissingAttribute failure = new MissingAttribute("A task, fixed rate, repeat interval, or time must be specified on the timer component",
                                                            startLocation);
            context.addError(failure);
        }
    }

    private boolean processImplementationClass(TimerImplementation implementation, XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String implClass = reader.getAttributeValue(null, "class");
        if (implClass == null) {
            MissingAttribute failure = new MissingAttribute("The class attribute was not specified", startLocation);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return false;
        }
        implementation.setImplementationClass(implClass);
        try {
            Class<?> clazz = context.getClassLoader().loadClass(implClass);
            if (!(Runnable.class.isAssignableFrom(clazz))) {
                InvalidTimerInterface failure = new InvalidTimerInterface(clazz, implementation);
                context.addError(failure);
                LoaderUtil.skipToEndElement(reader);
                return false;

            }
        } catch (ClassNotFoundException e) {
            ImplementationArtifactNotFound failure = new ImplementationArtifactNotFound(implClass, e.getMessage(), implementation);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return false;
        }
        return true;
    }

    private void processIntervalClass(XMLStreamReader reader, IntrospectionContext context, TimerImplementation implementation) throws XMLStreamException {
        TimerData data = implementation.getTimerData();
        String intervalClass = reader.getAttributeValue(null, "intervalClass");
        if (intervalClass == null) {
            // no task defined
            return;
        }
        try {
            data.setType(TimerType.RECURRING);
            Class<?> clazz = context.getClassLoader().loadClass(intervalClass);
            try {
                clazz.getMethod("nextInterval");
            } catch (NoSuchMethodException e) {
                InvalidIntervalClass failure = new InvalidIntervalClass(clazz, implementation);
                context.addError(failure);
            }
        } catch (ClassNotFoundException e) {
            ImplementationArtifactNotFound failure = new ImplementationArtifactNotFound(intervalClass, e.getMessage(), implementation);
            context.addError(failure);
        }

        data.setIntervalClass(intervalClass);
    }

    private void processIntervalMethod(IntrospectionContext context, TimerImplementation implementation) throws XMLStreamException {
        try {
            String name = implementation.getImplementationClass();
            Class<?> clazz = context.getClassLoader().loadClass(name);
            Method intervalMethod = clazz.getMethod("nextInterval");
            Class<?> type = intervalMethod.getReturnType();
            TimerData data = implementation.getTimerData();
            data.setIntervalMethod(true);  // set regardless of whether the method is valid
            data.setType(TimerType.RECURRING);
            if (!Long.class.equals(type) && !Long.TYPE.equals(type)) {
                InvalidIntervalMethod failure = new InvalidIntervalMethod("The nextInterval method must return a long value: " + name, clazz, implementation);
                context.addError(failure);
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // this should not happen as the impl class should already be loaded
        }
    }

    private void processRepeatInterval(XMLStreamReader reader,
                                       Location startLocation,
                                       IntrospectionContext introspectionContext,
                                       TimerImplementation implementation) {
        TimerData data = implementation.getTimerData();
        String repeatInterval = reader.getAttributeValue(null, "repeatInterval");
        if (repeatInterval != null) {
            if (data.getIntervalClass() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("A task and repeat interval are both specified", startLocation, implementation);
                introspectionContext.addError(failure);
            }
            try {
                long repeat = Long.parseLong(repeatInterval);
                data.setType(TimerType.INTERVAL);
                data.setRepeatInterval(repeat);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval is invalid: " + repeatInterval, startLocation, e, implementation);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processRepeatFixedRate(XMLStreamReader reader,
                                        Location startLocation,
                                        IntrospectionContext introspectionContext,
                                        TimerImplementation implementation) {
        TimerData data = implementation.getTimerData();
        String fixedRate = reader.getAttributeValue(null, "fixedRate");
        if (fixedRate != null) {
            if (data.getIntervalClass() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("A task and fixed rate are both specified", startLocation, implementation);
                introspectionContext.addError(failure);
            }
            if (data.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fixed rate are both specified", startLocation, implementation);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(fixedRate);
                data.setType(TimerType.FIXED_RATE);
                data.setFixedRate(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Fixed rate interval is invalid: " + fixedRate, startLocation, e, implementation);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processFireOnce(XMLStreamReader reader,
                                 Location startLocation,
                                 IntrospectionContext introspectionContext,
                                 TimerImplementation implementation) {
        TimerData data = implementation.getTimerData();
        String time = reader.getAttributeValue(null, "fireOnce");
        if (time != null) {
            if (data.getIntervalClass() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("A task and fire once are both specified", startLocation, implementation);
                introspectionContext.addError(failure);
            }
            if (data.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fire once are both specified", startLocation, implementation);
                introspectionContext.addError(failure);
            }
            if (data.getFixedRate() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Fixed rate and fire once are both specified", startLocation, implementation);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(time);
                data.setType(TimerType.ONCE);
                data.setFireOnce(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Fire once time is invalid: " + time, startLocation, e, implementation);
                introspectionContext.addError(failure);
            }
        }
    }

}