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
package org.fabric3.implementation.junit.introspection;

import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;

import org.fabric3.api.Namespaces;
import org.fabric3.api.implementation.junit.Fabric3Runner;
import org.fabric3.spi.introspection.java.ComponentAnnotationMapper;
import org.junit.runner.RunWith;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Maps the {@link RunWith} annotation configured with the {@link Fabric3Runner} class to the JUnit component implementation type.
 */
@EagerInit
public class JUnitComponentAnnotationMapper implements ComponentAnnotationMapper {
    private static final QName JUNIT = new QName(Namespaces.F3, "junit");

    public QName getImplementationType(Annotation annotation) {
        if (!(annotation instanceof RunWith)) {
            return null;
        }
        RunWith runWith = (RunWith) annotation;
        if (!Fabric3Runner.class.equals(runWith.value())) {
            return null;
        }
        return JUNIT;
    }
}
