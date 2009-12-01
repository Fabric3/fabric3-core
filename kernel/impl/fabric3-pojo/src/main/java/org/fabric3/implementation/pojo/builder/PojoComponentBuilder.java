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
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
import org.fabric3.spi.component.Component;
import org.fabric3.spi.expression.ExpressionExpander;
import org.fabric3.spi.expression.ExpressionExpansionException;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.model.physical.PhysicalPropertyDefinition;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectableType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import static org.fabric3.spi.model.type.xsd.XSDConstants.PROPERTY_TYPE;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.ParamTypes;

/**
 * Base class for ComponentBuilders that build components based on POJOs.
 *
 * @version $Rev$ $Date$
 */
public abstract class PojoComponentBuilder<T, PCD extends PojoComponentDefinition, C extends Component> implements ComponentBuilder<PCD, C> {

    protected ClassLoaderRegistry classLoaderRegistry;
    protected TransformerRegistry transformerRegistry;
    protected ExpressionExpander expander;

    protected IntrospectionHelper helper;

    protected PojoComponentBuilder(ClassLoaderRegistry classLoaderRegistry, TransformerRegistry transformerRegistry, IntrospectionHelper helper) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.transformerRegistry = transformerRegistry;
        this.helper = helper;
    }

    /**
     * Optional ExpressionExpander for substituting values for properties containing expressions of the form '${..}'. Values may be sourced from a
     * variety of places, including a file or system property.
     *
     * @param expander the injected expander
     */
    @Reference(required = false)
    public void setExpander(ExpressionExpander expander) {
        this.expander = expander;
    }

    protected void createPropertyFactories(PCD definition, InstanceFactoryProvider<T> provider) throws BuilderException {
        List<PhysicalPropertyDefinition> propertyDefinitions = definition.getPropertyDefinitions();

        TypeMapping typeMapping = new TypeMapping();
        helper.resolveTypeParameters(provider.getImplementationClass(), typeMapping);

        for (PhysicalPropertyDefinition propertyDefinition : propertyDefinitions) {
            String name = propertyDefinition.getName();
            Document value = propertyDefinition.getValue();
            Element element = value.getDocumentElement();
            Injectable source = new Injectable(InjectableType.PROPERTY, name);

            Type type = provider.getGenericType(source);
            DataType<?> dataType = getDataType(type, typeMapping);

            ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());
            ObjectFactory<?> objectFactory = createObjectFactory(name, dataType, element, classLoader);
            provider.setObjectFactory(source, objectFactory);
        }
    }

    protected void buildContexts(PojoComponent component, InstanceFactoryProvider<T> provider) {
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

    @SuppressWarnings("unchecked")
    private ObjectFactory<?> createObjectFactory(String name, DataType<?> dataType, Element value, ClassLoader classLoader) throws BuilderException {
        try {
            Class<?> physical = dataType.getPhysical();
            List<Class<?>> types = new ArrayList<Class<?>>();
            types.add(physical);
            Transformer<Node, ?> transformer = (Transformer<Node, ?>) transformerRegistry.getTransformer(PROPERTY_TYPE, dataType, types, types);
            if (transformer == null) {
                throw new PropertyTransformException("No transformer for property " + name + " of type: " + dataType);
            }
            Object instance = transformer.transform(value, classLoader);
            if (instance instanceof String && expander != null) {
                // if the property value is a string, expand it if it contains expressions
                instance = expander.expand((String) instance);
            }
            return new SingletonObjectFactory(instance);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, e);
        } catch (ExpressionExpansionException e) {
            throw new PropertyTransformException("Unable to expand property value: " + name, e);
        }

    }

}
