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
package org.fabric3.implementation.pojo.builder;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.ManagementInfo;
import org.fabric3.implementation.pojo.component.PojoComponentContext;
import org.fabric3.implementation.pojo.component.PojoRequestContext;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.provision.PhysicalPojoComponent;
import org.fabric3.spi.container.builder.component.ComponentBuilder;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.model.physical.ParamTypes;
import org.fabric3.spi.model.physical.PhysicalProperty;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.w3c.dom.Document;

/**
 * Base class for component builders that create Java-based components.
 */
public abstract class PojoComponentBuilder<PCD extends PhysicalPojoComponent, C extends Component> implements ComponentBuilder<PCD, C> {
    protected IntrospectionHelper helper;
    private HostInfo info;
    private PropertySupplierBuilder propertyBuilder;
    private ManagementService managementService;

    protected PojoComponentBuilder(PropertySupplierBuilder propertyBuilder, ManagementService managementService, IntrospectionHelper helper, HostInfo info) {
        this.propertyBuilder = propertyBuilder;
        this.managementService = managementService;
        this.helper = helper;
        this.info = info;
    }

    protected void createPropertyFactories(PCD definition, ImplementationManagerFactory factory) throws Fabric3Exception {
        List<PhysicalProperty> properties = definition.getProperties();

        TypeMapping typeMapping = new TypeMapping();
        helper.resolveTypeParameters(factory.getImplementationClass(), typeMapping);

        for (PhysicalProperty property : properties) {
            String name = property.getName();
            Injectable source = new Injectable(InjectableType.PROPERTY, name);
            if (property.getInstanceValue() != null) {
                factory.setSupplier(source, property::getInstanceValue);
            } else {
                Document value = property.getValue();

                Type type = factory.getGenericType(source);
                DataType dataType = getDataType(type, typeMapping);

                ClassLoader classLoader = factory.getImplementationClass().getClassLoader();
                boolean many = property.isMany();
                factory.setSupplier(source, propertyBuilder.createSupplier(name, dataType, value, many, classLoader));
            }
        }
    }

    protected void export(PhysicalPojoComponent pojoComponent, AtomicComponent component) throws Fabric3Exception {
        if (pojoComponent.isManaged()) {
            ManagementInfo info = pojoComponent.getManagementInfo();
            URI uri = pojoComponent.getComponentUri();
            managementService.export(uri, info, component::createSupplier);
        }
    }

    protected void dispose(PhysicalPojoComponent pojoComponent) throws Fabric3Exception {
        if (pojoComponent.isManaged()) {
            ManagementInfo info = pojoComponent.getManagementInfo();
            URI uri = pojoComponent.getComponentUri();
            managementService.remove(uri, info);
        }
    }

    protected void buildContexts(org.fabric3.implementation.pojo.component.PojoComponent component, ImplementationManagerFactory factory) {
        PojoRequestContext requestContext = new PojoRequestContext();
        factory.setSupplier(Injectable.OASIS_REQUEST_CONTEXT, () -> requestContext);
        PojoComponentContext componentContext = new PojoComponentContext(component, requestContext, info);
        factory.setSupplier(Injectable.OASIS_COMPONENT_CONTEXT, () -> componentContext);
    }

    @SuppressWarnings({"unchecked"})
    private DataType getDataType(Type type, TypeMapping typeMapping) {
        if (type instanceof Class) {
            // non-generic type
            Class<?> nonGenericType = (Class<?>) type;
            if (nonGenericType.isPrimitive()) {
                // convert primitive representation to its object equivalent
                nonGenericType = ParamTypes.PRIMITIVE_TO_OBJECT.get(nonGenericType);
            }
            return new JavaType(nonGenericType);
        } else {
            // a generic
            JavaTypeInfo info = helper.createTypeInfo(type, typeMapping);
            return new JavaGenericType(info);

        }
    }

}
