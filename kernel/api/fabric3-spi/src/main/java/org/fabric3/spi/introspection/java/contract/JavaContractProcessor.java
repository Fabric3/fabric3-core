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
package org.fabric3.spi.introspection.java.contract;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 * Introspects a JavaServiceContract from a Java type.
 */
public interface JavaContractProcessor {
    /**
     * Introspects a Java class and return the JavaServiceContract. If validation errors or warnings are encountered, they will be reported in the
     * IntrospectionContext.
     *
     * @param interfaze    the Java class to introspect
     * @param context      the introspection context for reporting errors and warnings
     * @param modelObjects the parent model objects. May be null.
     * @return the ServiceContract corresponding to the interface type
     */
    JavaServiceContract introspect(Class<?> interfaze, IntrospectionContext context, ModelObject... modelObjects);

    /**
     * Introspects a generic Java class and return the JavaServiceContract. If validation errors or warnings are encountered, they will be reported in
     * the IntrospectionContext.
     *
     * @param interfaze    the Java class to introspect
     * @param baseClass    the base class to use for introspecting and resolving generic formal types to actual types. For example, a service contract
     *                     on a reference may contain a formal type declaration (e.g. T) that is defined by the implementation class where the
     *                     reference is injected. The base class may also be the same as the interface to be introspected in cases where a service
     *                     contract is not associated with an implementation class.
     * @param context      the introspection context for reporting errors and warnings
     * @param modelObjects the parent model objects. May be null.
     * @return the ServiceContract corresponding to the interface type
     */
    JavaServiceContract introspect(Class<?> interfaze, Class<?> baseClass, IntrospectionContext context, ModelObject... modelObjects);

}
