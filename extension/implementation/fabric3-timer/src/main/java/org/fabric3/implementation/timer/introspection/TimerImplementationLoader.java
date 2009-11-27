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
*/
package org.fabric3.implementation.timer.introspection;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.implementation.java.introspection.JavaImplementationProcessor;
import org.fabric3.implementation.java.introspection.ImplementationArtifactNotFound;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.implementation.timer.model.TimerImplementation;
import org.fabric3.implementation.timer.provision.TriggerData;
import static org.fabric3.implementation.timer.provision.TriggerData.UNSPECIFIED;
import org.fabric3.implementation.timer.provision.TriggerType;

/**
 * Loads <implementation.timer> entries in a composite.
 */
public class TimerImplementationLoader implements TypeLoader<TimerImplementation> {
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("class", "class");
        ATTRIBUTES.put("cronExpression", "cronExpression");
        ATTRIBUTES.put("fixedRate", "fixedRate");
        ATTRIBUTES.put("repeatInterval", "repeatInterval");
        ATTRIBUTES.put("fireOnce", "fireOnce");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
    }

    private final JavaImplementationProcessor implementationProcessor;
    private final LoaderHelper loaderHelper;


    public TimerImplementationLoader(@Reference JavaImplementationProcessor implementationProcessor, @Reference LoaderHelper loaderHelper) {
        this.implementationProcessor = implementationProcessor;
        this.loaderHelper = loaderHelper;
    }


    public TimerImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        assert TimerImplementation.IMPLEMENTATION_TIMER.equals(reader.getName());

        validateAttributes(reader, context);
        TimerImplementation implementation = new TimerImplementation();
        if (!processImplementationClass(implementation, reader, context)) {
            // an error with the implementation class, return a dummy implementation
            InjectingComponentType type = new InjectingComponentType(null);
            implementation.setComponentType(type);
            return implementation;
        }
        TriggerData data = new TriggerData();
        implementation.setTriggerData(data);
        processCronExpression(reader, context, data);
        processRepeatInterval(reader, context, data);
        processRepeatFixedRate(reader, context, data);
        processFireOnce(reader, context, data);
        validateFireData(reader, context, data);
        loaderHelper.loadPolicySetsAndIntents(implementation, reader, context);

        LoaderUtil.skipToEndElement(reader);

        implementationProcessor.introspect(implementation, context);
        return implementation;
    }

    private void validateFireData(XMLStreamReader reader, IntrospectionContext context, TriggerData data) {
        if (data.getCronExpression() == null
                && data.getFixedRate() == UNSPECIFIED
                && data.getRepeatInterval() == UNSPECIFIED
                && data.getFireOnce() == UNSPECIFIED) {
            MissingAttribute failure =
                    new MissingAttribute("A cron expression, fixed rate, repeat interval, or fire once time must be specified on the timer component",
                                         reader);
            context.addError(failure);
        }
    }

    private boolean processImplementationClass(TimerImplementation implementation, XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException {

        String implClass = reader.getAttributeValue(null, "class");
        if (implClass == null) {
            MissingAttribute failure = new MissingAttribute("The class attribute was not specified", reader);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return false;
        }
        implementation.setImplementationClass(implClass);
        try {
            Class<?> clazz = context.getClassLoader().loadClass(implClass);
            if (!(Runnable.class.isAssignableFrom(clazz))) {
                InvalidInterface failure = new InvalidInterface(implementation);
                context.addError(failure);
                LoaderUtil.skipToEndElement(reader);
                return true;  // have processing continue

            }
        } catch (ClassNotFoundException e) {
            ImplementationArtifactNotFound failure = new ImplementationArtifactNotFound(implClass, e.getMessage());
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return false;
        }
        return true;
    }

    private void processCronExpression(XMLStreamReader reader, IntrospectionContext introspectionContext, TriggerData data) {
        String cronExpression = reader.getAttributeValue(null, "cronExpression");
        if (cronExpression != null) {
            try {
                new CronExpression(cronExpression);
                data.setType(TriggerType.CRON);
                data.setCronExpression(cronExpression);
            } catch (ParseException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Cron expression is invalid: " + cronExpression, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processRepeatInterval(XMLStreamReader reader, IntrospectionContext introspectionContext, TriggerData data) {
        String repeatInterval = reader.getAttributeValue(null, "repeatInterval");
        if (repeatInterval != null) {
            if (data.getCronExpression() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Cron expression and repeat interval both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long repeat = Long.parseLong(repeatInterval);
                data.setType(TriggerType.INTERVAL);
                data.setRepeatInterval(repeat);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Repeat interval is invalid: " + repeatInterval, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processRepeatFixedRate(XMLStreamReader reader, IntrospectionContext introspectionContext, TriggerData data) {
        String fixedRate = reader.getAttributeValue(null, "fixedRate");
        if (fixedRate != null) {
            if (data.getCronExpression() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Cron expression and fixed rate both specified", reader);
                introspectionContext.addError(failure);
            }
            if (data.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fixed rate both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(fixedRate);
                data.setType(TriggerType.FIXED_RATE);
                data.setFixedRate(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Fixed rate interval is invalid: " + fixedRate, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void processFireOnce(XMLStreamReader reader, IntrospectionContext introspectionContext, TriggerData data) {
        String time = reader.getAttributeValue(null, "fireOnce");
        if (time != null) {
            if (data.getCronExpression() != null) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Cron expression and fire once both specified", reader);
                introspectionContext.addError(failure);
            }
            if (data.getRepeatInterval() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Repeat interval and fire once both specified", reader);
                introspectionContext.addError(failure);
            }
            if (data.getFixedRate() != UNSPECIFIED) {
                InvalidTimerExpression failure = new InvalidTimerExpression("Ficed rate and fire once both specified", reader);
                introspectionContext.addError(failure);
            }
            try {
                long rate = Long.parseLong(time);
                data.setType(TriggerType.ONCE);
                data.setFireOnce(rate);
            } catch (NumberFormatException e) {
                InvalidTimerExpression failure =
                        new InvalidTimerExpression("Fire once time is invalid: " + time, reader, e);
                introspectionContext.addError(failure);
            }
        }
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}