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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.builder;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import org.w3c.dom.Document;

import org.fabric3.implementation.pojo.component.OASISPojoComponentContext;
import org.fabric3.implementation.pojo.component.OASISPojoRequestContext;
import org.fabric3.implementation.pojo.component.PojoComponent;
import org.fabric3.implementation.pojo.component.PojoComponentContext;
import org.fabric3.implementation.pojo.component.PojoRequestContext;
import org.fabric3.implementation.pojo.injection.ConversationIDObjectFactory;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.implementation.pojo.provision.PojoComponentDefinition;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.model.physical.PhysicalPropertyDefinition;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectableType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.fabric3.spi.model.type.java.ManagementInfo;
import org.fabric3.spi.util.ParamTypes;

/**
 * Base class for component builders that build Java-based components.
 *
 * @version $Rev$ $Date$
 */
public abstract class PojoComponentBuilder<PCD extends PojoComponentDefinition, C extends Component> implements ComponentBuilder<PCD, C> {
    protected ClassLoaderRegistry classLoaderRegistry;
    protected IntrospectionHelper helper;
    private PropertyObjectFactoryBuilder propertyBuilder;
    private ManagementService managementService;

    protected PojoComponentBuilder(ClassLoaderRegistry registry,
                                   PropertyObjectFactoryBuilder propertyBuilder,
                                   ManagementService managementService,
                                   IntrospectionHelper helper) {
        this.classLoaderRegistry = registry;
        this.propertyBuilder = propertyBuilder;
        this.managementService = managementService;
        this.helper = helper;
    }

    protected void createPropertyFactories(PCD definition, InstanceFactoryProvider provider) throws BuilderException {
        List<PhysicalPropertyDefinition> propertyDefinitions = definition.getPropertyDefinitions();

        TypeMapping typeMapping = new TypeMapping();
        helper.resolveTypeParameters(provider.getImplementationClass(), typeMapping);

        for (PhysicalPropertyDefinition propertyDefinition : propertyDefinitions) {
            String name = propertyDefinition.getName();
            Document value = propertyDefinition.getValue();
            Injectable source = new Injectable(InjectableType.PROPERTY, name);

            Type type = provider.getGenericType(source);
            DataType<?> dataType = getDataType(type, typeMapping);

            ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());
            boolean many = propertyDefinition.isMany();
            ObjectFactory<?> objectFactory = propertyBuilder.createObjectFactory(name, dataType, value, many, classLoader);
            provider.setObjectFactory(source, objectFactory);
        }
    }

    protected void export(PojoComponentDefinition definition, ClassLoader classLoader, AtomicComponent component) throws BuilderException {
        if (definition.isManaged()) {
            ManagementInfo info = definition.getManagementInfo();
            ObjectFactory<Object> objectFactory = component.createObjectFactory();
            try {
                URI uri = definition.getComponentUri();
                managementService.export(uri, info, objectFactory, classLoader);
            } catch (ManagementException e) {
                throw new BuilderException(e);
            }
        }
    }

    protected void dispose(PojoComponentDefinition definition) throws BuilderException {
        if (definition.isManaged()) {
            ManagementInfo info = definition.getManagementInfo();
            try {
                URI uri = definition.getComponentUri();
                managementService.remove(uri, info);
            } catch (ManagementException e) {
                throw new BuilderException(e);
            }
        }
    }

    protected void buildContexts(PojoComponent component, InstanceFactoryProvider provider) {
        PojoRequestContext requestContext = new PojoRequestContext();
        SingletonObjectFactory<PojoRequestContext> requestObjectFactory = new SingletonObjectFactory<PojoRequestContext>(requestContext);
        provider.setObjectFactory(Injectable.REQUEST_CONTEXT, requestObjectFactory);
        PojoComponentContext componentContext = new PojoComponentContext(component, requestContext);
        SingletonObjectFactory<PojoComponentContext> componentObjectFactory = new SingletonObjectFactory<PojoComponentContext>(componentContext);
        provider.setObjectFactory(Injectable.COMPONENT_CONTEXT, componentObjectFactory);
        ConversationIDObjectFactory conversationIDObjectFactory = new ConversationIDObjectFactory();
        provider.setObjectFactory(Injectable.CONVERSATION_ID, conversationIDObjectFactory);

        OASISPojoRequestContext oasisRequestContext = new OASISPojoRequestContext();
        SingletonObjectFactory<OASISPojoRequestContext> oasisRequestFactory =
                new SingletonObjectFactory<OASISPojoRequestContext>(oasisRequestContext);
        provider.setObjectFactory(Injectable.OASIS_REQUEST_CONTEXT, oasisRequestFactory);
        OASISPojoComponentContext oasisComponentContext = new OASISPojoComponentContext(component, oasisRequestContext);
        SingletonObjectFactory<OASISPojoComponentContext> oasisComponentFactory =
                new SingletonObjectFactory<OASISPojoComponentContext>(oasisComponentContext);
        provider.setObjectFactory(Injectable.OASIS_COMPONENT_CONTEXT, oasisComponentFactory);

    }

    @SuppressWarnings({"unchecked"})
    private DataType<?> getDataType(Type type, TypeMapping typeMapping) {
        if (type instanceof Class) {
            // non-generic type
            Class<?> nonGenericType = (Class<?>) type;
            if (nonGenericType.isPrimitive()) {
                // convert primitive representation to its object equivalent
                nonGenericType = ParamTypes.PRIMITIVE_TO_OBJECT.get(nonGenericType);
            }
            return new JavaClass(nonGenericType);
        } else {
            // a generic
            JavaTypeInfo info = helper.createTypeInfo(type, typeMapping);
            return new JavaGenericType(info);

        }
    }

}
