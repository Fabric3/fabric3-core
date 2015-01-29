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
package org.fabric3.jpa.introspection;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.jpa.model.PersistenceUnitResourceReference;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class PersistenceUnitProcessor extends AbstractAnnotationProcessor<PersistenceUnit> {

    private final ServiceContract factoryServiceContract;

    public PersistenceUnitProcessor(@Reference JavaContractProcessor contractProcessor) {
        super(PersistenceUnit.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        factoryServiceContract = contractProcessor.introspect(EntityManagerFactory.class, context);
        assert !context.hasErrors(); // should not happen
    }

    public void visitField(PersistenceUnit annotation,
                           Field field,
                           Class<?> implClass,
                           InjectingComponentType componentType,
                           IntrospectionContext context) {
        FieldInjectionSite site = new FieldInjectionSite(field);
        PersistenceUnitResourceReference definition = createDefinition(annotation);
        componentType.add(definition, site);
    }

    public void visitMethod(PersistenceUnit annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        PersistenceUnitResourceReference definition = createDefinition(annotation);
        componentType.add(definition, site);
    }

    PersistenceUnitResourceReference createDefinition(PersistenceUnit annotation) {
        String name = annotation.name();
        String unitName = annotation.unitName();
        return new PersistenceUnitResourceReference(name, unitName, factoryServiceContract);
    }
}
