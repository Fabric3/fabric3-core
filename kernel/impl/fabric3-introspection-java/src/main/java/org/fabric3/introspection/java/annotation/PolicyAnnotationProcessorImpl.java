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
package org.fabric3.introspection.java.annotation;

import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.api.annotation.IntentMetaData;
import org.fabric3.api.model.type.PolicyAware;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.oasisopen.sca.annotation.Intent;
import org.oasisopen.sca.annotation.PolicySets;
import org.oasisopen.sca.annotation.Qualifier;
import org.oasisopen.sca.annotation.Requires;

/**
 *
 */
public class PolicyAnnotationProcessorImpl implements PolicyAnnotationProcessor {

    public void process(Annotation annotation, PolicyAware modelObject, IntrospectionContext context) {
        if (!(annotation instanceof Requires) && !(annotation instanceof PolicySets)) {
            processIntentAnnotation(annotation, modelObject, context);
        }
    }

    /**
     * Evaluates an intent annotation.
     *
     * @param annotation  the intent annotation
     * @param modelObject the model object the intent is associated with
     * @param context     the current introspection context
     */
    private void processIntentAnnotation(Annotation annotation, PolicyAware modelObject, IntrospectionContext context) {
        Class<? extends Annotation> annotClass = annotation.annotationType();
        try {
            if (annotClass.isAnnotationPresent(Intent.class)) {
                Intent intent = annotClass.getAnnotation(Intent.class);
                String val = intent.value();
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
                if (modelObject instanceof ComponentType) {
                    modelObject.addPolicy(name.getLocalPart());
                } else if (modelObject instanceof Operation) {
                    Operation operation = (Operation) modelObject;
                    operation.addPolicy(name.getLocalPart());
                    String[] metadata = getMetadataValue(annotation, IntentMetaData.class, context);
                    if (metadata != null && metadata.length >= 1 && metadata[0].length() >= 1) {
                        operation.addMetadata(name, metadata);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            context.addError(new InvalidAnnotation("Error reading annotation value ", annotClass, annotation, annotClass, e));
        } catch (IllegalAccessException e) {
            context.addError(new InvalidAnnotation("Error reading annotation value " + annotClass.getName(), annotClass, annotation, annotClass, e));
        } catch (InvocationTargetException e) {
            context.addError(new InvalidAnnotation("Error reading annotation value" + annotClass.getName(), annotClass, annotation, annotClass, e));
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
                    InvalidAnnotation error = new InvalidAnnotation("Value for " + metadataName + " must be String or String[] on " + annotationName,
                                                                    annotClass,
                                                                    annotation,
                                                                    annotClass);
                    context.addError(error);
                }
            }
        }
        return null;
    }

}