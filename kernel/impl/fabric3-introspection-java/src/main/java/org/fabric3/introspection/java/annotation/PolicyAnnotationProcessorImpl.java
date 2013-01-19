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
package org.fabric3.introspection.java.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Intent;
import org.oasisopen.sca.annotation.PolicySets;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Qualifier;
import org.oasisopen.sca.annotation.Requires;

import org.fabric3.api.annotation.IntentMetaData;
import org.fabric3.model.type.PolicyAware;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;

/**
 *
 */
public class PolicyAnnotationProcessorImpl implements PolicyAnnotationProcessor {
    private Map<String, QName> intentsToQualifiers = new HashMap<String, QName>();

    @Property(required = false)
    public void setIntentsToQualifiers(Map<String, QName> intentsToQualifiers) {
        this.intentsToQualifiers = intentsToQualifiers;
    }

    public void process(Annotation annotation, PolicyAware modelObject, IntrospectionContext context) {
        if (annotation instanceof Requires) {
            processRequires((Requires) annotation, modelObject, context);
        } else if (annotation instanceof PolicySets) {
            processPolicySets((PolicySets) annotation, modelObject, context);
        } else {
            processIntentAnnotation(annotation, modelObject, context);
        }
    }

    /**
     * Evaluates an requires annotation.
     *
     * @param annotation  the requires annotation
     * @param modelObject the model object the requires annotation is associated with
     * @param context     the current introspection context
     */
    private void processRequires(Requires annotation, PolicyAware modelObject, IntrospectionContext context) {
        String[] intents = annotation.value();
        for (String intent : intents) {
            try {
                QName qName = QName.valueOf(intent);
                modelObject.addIntent(qName);
            } catch (IllegalArgumentException e) {
                InvalidIntentName error = new InvalidIntentName(intent, annotation.getClass(), e);
                context.addError(error);
            }
        }
    }

    /**
     * Evaluates a policy set annotation.
     *
     * @param annotation  the policy set annotation
     * @param modelObject the model object the policy set is associated with
     * @param context     the current introspection context
     */
    private void processPolicySets(PolicySets annotation, PolicyAware modelObject, IntrospectionContext context) {
        String[] policySets = annotation.value();
        for (String set : policySets) {
            try {
                QName qName = QName.valueOf(set);
                modelObject.addPolicySet(qName);
            } catch (IllegalArgumentException e) {
                InvalidIntentName error = new InvalidIntentName(set, null, e);
                context.addError(error);
            }
        }
    }

    /**
     * Evaluates an intent annotation.
     *
     * @param annotation  the intent annotation
     * @param modelObject the model object the intent is associated with
     * @param context     the current introspection context
     */
    private void processIntentAnnotation(Annotation annotation,
                                         PolicyAware modelObject,
                                         IntrospectionContext context) {
        Class<? extends Annotation> annotClass = annotation.annotationType();
        if (annotClass.isAnnotationPresent(Intent.class)) {
            Intent intent = annotClass.getAnnotation(Intent.class);
            String val = intent.value();
            try {
                String[] qualifiers = getMetadataValue(annotation, Qualifier.class, context);
                QName name = null;
                if (qualifiers == null || qualifiers.length < 1 || qualifiers[0].length() < 1) {
                    // no qualifiers
                    name = QName.valueOf(val);
                } else {
                    for (String qualifier : qualifiers) {
                        name = QName.valueOf(qualifier);
                    }
                }
                modelObject.addIntent(name);
                String[] metadata = getMetadataValue(annotation, IntentMetaData.class, context);
                if (metadata != null && metadata.length >= 1 && metadata[0].length() >= 1) {
                    modelObject.addMetadata(name, metadata);
                }
            } catch (IllegalArgumentException e) {
                context.addError(new InvalidIntentName(val, annotClass, e));
            } catch (IllegalAccessException e) {
                context.addError(new InvalidAnnotation("Error reading annotation value " + annotClass.getName(), annotClass, e));
            } catch (InvocationTargetException e) {
                context.addError(new InvalidAnnotation("Error reading annotation value" + annotClass.getName(), annotClass, e));
            }
        } else {
            // check if the annotation is an intent annotation but not marked with the @Intent annotation
            QName qualifier = intentsToQualifiers.get(annotation.annotationType().getName());
            if (qualifier != null) {
                modelObject.addIntent(qualifier);
            }
        }
    }

    /**
     * Evaluates a metadata annotation on an intent annotation, e.g. @Qualifier or @IntentMetaData. Currently only String and String[] are supported.
     *
     * @param annotation         the intent annotation
     * @param metadataAnnotClass the metadata annotation class
     * @param context            the current introspection context
     * @return the metadata values the metadata
     * @throws IllegalAccessException    if an error occurs reading the annotation metadata
     * @throws InvocationTargetException if an error occurs reading the annotation metadata
     */
    private String[] getMetadataValue(Annotation annotation, Class<? extends Annotation> metadataAnnotClass, IntrospectionContext context)
            throws IllegalAccessException, InvocationTargetException {
        Class<? extends Annotation> annotClass = annotation.annotationType();
        for (Method method : annotClass.getMethods()) {
            if (method.isAnnotationPresent(metadataAnnotClass)) {
                // iterate methods until one with @Qualified is found
                Class<?> type = method.getReturnType();
                if (type.isArray() && (String.class.equals(type.getComponentType()))) {
                    // multiple qualifiers as return type String[]
                    return (String[]) method.invoke(annotation);
                } else if (String.class.equals(type)) {
                    // single qualifier as return type String[]
                    String ret = (String) method.invoke(annotation);
                    return new String[]{ret};
                } else {
                    String metadataName = metadataAnnotClass.getName();
                    String annotationName = annotClass.getName();
                    InvalidAnnotation error =
                            new InvalidAnnotation("Value for " + metadataName + " must be String or String[] on " + annotationName, annotClass);
                    context.addError(error);
                }
            }
        }
        return null;
    }


}