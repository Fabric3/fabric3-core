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
package org.fabric3.spi.introspection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * Denotes an invalid use of an annotation.
 */
public class InvalidAnnotation extends JavaValidationFailure {
    private String message;
    private AnnotatedElement element;
    private Annotation annotation;
    private Exception e;

    public InvalidAnnotation(String message, AnnotatedElement element, Annotation annotation, Class<?> clazz) {
        super(clazz);
        this.message = message;
        this.element = element;
        this.annotation = annotation;
    }

    public InvalidAnnotation(String message, AnnotatedElement element, Annotation annotation, Class<?> clazz, Exception e) {
        super(clazz);
        this.message = message;
        this.element = element;
        this.annotation = annotation;
        this.e = e;
    }

    public AnnotatedElement getElement() {
        return element;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public String getMessage() {
        if (e == null) {
            return message + " : " + getCodeLocation();
        }
        return message + ".\n" + e;
    }

    public String getShortMessage() {
        if (e == null) {
            return message;
        }
        return message + ": " + e.getMessage();
    }

}