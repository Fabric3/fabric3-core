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
package org.fabric3.spi.introspection.java.annotation;

import java.lang.annotation.Annotation;

import org.fabric3.api.annotation.IntentMetaData;
import org.fabric3.api.model.type.PolicyAware;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Parses intent and policy set annotations (i.e. annotations marked with @Intent) and updates the model object they are attached to. Implementations must also
 * support the Fabric3 annotation {@link IntentMetaData}.
 */
public interface PolicyAnnotationProcessor {

    /**
     * Process the annotation.
     *
     * @param annotation  the annotation
     * @param modelObject the model object
     * @param context     the current introspection context.
     */
    void process(Annotation annotation, PolicyAware modelObject, IntrospectionContext context);

}
