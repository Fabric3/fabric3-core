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
package org.fabric3.implementation.system.introspection;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Controls the order in which system implementation heuristics are applied.
 */
public class SystemHeuristic implements HeuristicProcessor {
    private final HeuristicProcessor serviceHeuristic;
    private final HeuristicProcessor constructorHeuristic;
    private final HeuristicProcessor injectionHeuristic;

    public SystemHeuristic(@Reference(name = "service") HeuristicProcessor serviceHeuristic,
                           @Reference(name = "constructor") HeuristicProcessor constructorHeuristic,
                           @Reference(name = "injection") HeuristicProcessor injectionHeuristic) {
        this.serviceHeuristic = serviceHeuristic;
        this.constructorHeuristic = constructorHeuristic;
        this.injectionHeuristic = injectionHeuristic;
    }

    public void applyHeuristics(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        serviceHeuristic.applyHeuristics(componentType, implClass, context);
        constructorHeuristic.applyHeuristics(componentType, implClass, context);
        injectionHeuristic.applyHeuristics(componentType, implClass, context);
    }
}
