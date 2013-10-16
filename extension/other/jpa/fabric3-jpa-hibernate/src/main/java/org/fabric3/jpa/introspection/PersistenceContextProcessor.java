/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.jpa.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.jpa.model.HibernateSessionResourceReference;
import org.fabric3.jpa.model.PersistenceContextResourceReference;
import org.fabric3.model.type.component.Scope;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 * Processes @PersistenceContext annotations.
 */
@EagerInit
public class PersistenceContextProcessor extends AbstractAnnotationProcessor<PersistenceContext> {
    private ServiceContract factoryServiceContract;
    private IntrospectionHelper helper;

    public PersistenceContextProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(PersistenceContext.class);
        this.helper = helper;
        IntrospectionContext context = new DefaultIntrospectionContext();
        factoryServiceContract = contractProcessor.introspect(EntityManager.class, context);
        assert !context.hasErrors(); // should not happen
    }

    public void visitField(PersistenceContext annotation,
                           Field field,
                           Class<?> implClass,
                           InjectingComponentType componentType,
                           IntrospectionContext context) {
        FieldInjectionSite site = new FieldInjectionSite(field);
        String name = helper.getSiteName(field, null);
        if (EntityManager.class.equals(field.getType())) {
            PersistenceContextResourceReference definition = createDefinition(name, field, annotation, componentType, context);
            componentType.add(definition, site);
        } else {
            HibernateSessionResourceReference definition = createSessionDefinition(name, annotation, componentType);
            componentType.add(definition, site);
        }
        // record that the implementation requires JPA
        componentType.addRequiredCapability("jpa");
    }

    public void visitMethod(PersistenceContext annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        String name = helper.getSiteName(method, null);
        if (EntityManager.class.equals(method.getParameterTypes()[0])) {
            PersistenceContextResourceReference definition = createDefinition(name, method, annotation, componentType, context);
            componentType.add(definition, site);
        } else {
            HibernateSessionResourceReference definition = createSessionDefinition(name, annotation, componentType);
            componentType.add(definition, site);
        }
        // record that the implementation requires JPA
        componentType.addRequiredCapability("jpa");
    }

    private PersistenceContextResourceReference createDefinition(String name,
                                                                 Member member,
                                                                 PersistenceContext annotation,
                                                                 InjectingComponentType componentType,
                                                                 IntrospectionContext context) {
        String unitName = annotation.unitName();
        PersistenceContextType type = annotation.type();
        if (PersistenceContextType.EXTENDED == type) {
            InvalidPersistenceContextType error =
                    new InvalidPersistenceContextType("Extended persistence contexts not supported: " + unitName, member, componentType);
            context.addError(error);
        }
        boolean multiThreaded = Scope.COMPOSITE.getScope().equals(componentType.getScope());
        return new PersistenceContextResourceReference(name, unitName, factoryServiceContract, multiThreaded);
    }

    private HibernateSessionResourceReference createSessionDefinition(String name,
                                                                      PersistenceContext annotation,
                                                                      InjectingComponentType componentType) {
        String unitName = annotation.unitName();
        boolean multiThreaded = Scope.COMPOSITE.getScope().equals(componentType.getScope());
        return new HibernateSessionResourceReference(name, unitName, factoryServiceContract, multiThreaded);
    }

}