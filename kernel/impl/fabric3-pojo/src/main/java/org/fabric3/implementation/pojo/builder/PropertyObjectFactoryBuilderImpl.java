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
*/

package org.fabric3.implementation.pojo.builder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.ParamTypes;

import static org.fabric3.spi.model.type.xsd.XSDConstants.PROPERTY_TYPE;

/**
 * @version $Rev$ $Date$
 */
public class PropertyObjectFactoryBuilderImpl implements PropertyObjectFactoryBuilder {
    private TransformerRegistry transformerRegistry;

    public PropertyObjectFactoryBuilderImpl(@Reference TransformerRegistry transformerRegistry) {
        this.transformerRegistry = transformerRegistry;
    }

    public ObjectFactory<?> createObjectFactory(String name, DataType<?> dataType, Document value, boolean many, ClassLoader classLoader)
            throws BuilderException {
        Class<?> physical = dataType.getPhysical();
        if (physical.isArray()) {
            return createArrayFactory(name, dataType, value, classLoader);
        } else if (Map.class.equals(physical)) {
            return createMapFactory(name, (JavaGenericType) dataType, value, classLoader);
        } else if (List.class.equals(physical)) {
            return createListFactory(name, (JavaGenericType) dataType, value, classLoader);
        } else if (Set.class.equals(physical)) {
            return createSetFactory(name, (JavaGenericType) dataType, value, classLoader);
        } else if (LinkedList.class.equals(physical)) {
            return createLinkedListFactory(name, (JavaGenericType) dataType, value, classLoader);
        } else {
            return createDefaultObjectFactory(name, dataType, value, classLoader);
        }

    }

    private ObjectFactory<?> createDefaultObjectFactory(String name, DataType<?> dataType, Document value, ClassLoader classLoader)
            throws PropertyTransformException {
        try {
            Class<?> physical = dataType.getPhysical();
            List<Class<?>> types = new ArrayList<Class<?>>();
            types.add(physical);
            Transformer<Node, ?> transformer = getTransformer(name, PROPERTY_TYPE, dataType, types);
            Element element = (Element) value.getDocumentElement().getFirstChild();
            Object instance = transformer.transform(element, classLoader);
            return new SingletonObjectFactory<Object>(instance);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private ObjectFactory<Map> createMapFactory(String name, JavaGenericType dataType, Document value, ClassLoader classLoader)
            throws PropertyTransformException {
        try {
            List<JavaTypeInfo> typeInfos = dataType.getLogical().getParameterTypesInfos();
            if (typeInfos.size() < 2) {
                // programming error
                throw new PropertyTransformException("Map properties must have a key and value type");
            }
            Class<?> keyType = typeInfos.get(0).getRawType();
            List<Class<?>> keyTypes = new ArrayList<Class<?>>();
            keyTypes.add(keyType);
            Class<?> valueType = dataType.getLogical().getParameterTypesInfos().get(1).getRawType();
            List<Class<?>> valueTypes = new ArrayList<Class<?>>();
            valueTypes.add(valueType);

            Transformer<Node, ?> keyTransformer = getTransformer(name, PROPERTY_TYPE, new JavaClass(keyType), keyTypes);
            Transformer<Node, ?> valueTransformer = getTransformer(name, PROPERTY_TYPE, new JavaClass(valueType), valueTypes);

            Map<Object, Object> map = new HashMap<Object, Object>();
            Element root = value.getDocumentElement();
            NodeList nodes = root.getFirstChild().getChildNodes(); // first child is the <value> tag
            for (int i = 0; i < nodes.getLength(); i++) {
                Node entryNode = nodes.item(i);
                if (entryNode.getChildNodes().getLength() != 2) {
                    throw new PropertyTransformException("Invalid Map format: there must be a key and value node for property " + name);
                }
                Element keyNode = (Element) entryNode.getChildNodes().item(0);
                Element valNode = (Element) entryNode.getChildNodes().item(1);
                Object key = keyTransformer.transform(keyNode, classLoader);
                Object val = valueTransformer.transform(valNode, classLoader);
                map.put(key, val);
            }

            return new SingletonObjectFactory<Map>(map);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, e);
        }

    }

    @SuppressWarnings({"unchecked"})
    private ObjectFactory<?> createArrayFactory(String name, DataType dataType, Document value, ClassLoader classLoader)
            throws PropertyTransformException {
        try {
            Class componentType = dataType.getPhysical().getComponentType();
            Class<?> type = componentType;
            if (type.isPrimitive()) {
                type = ParamTypes.PRIMITIVE_TO_OBJECT.get(type);
            }

            List<Class<?>> types = new ArrayList<Class<?>>();
            types.add(type);

            Transformer<Node, ?> transformer = getTransformer(name, PROPERTY_TYPE, new JavaClass(type), types);

            Element root = value.getDocumentElement();
            NodeList nodes = root.getChildNodes();
            Object array = Array.newInstance(componentType, nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i).getFirstChild();
                Object val = transformer.transform(node, classLoader);
                Array.set(array, i, val);
            }
            return new SingletonObjectFactory<Object>(array);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, e);
        }
    }

    private <T> ObjectFactory<Collection<T>> createSetFactory(String name, JavaGenericType dataType, Document value, ClassLoader classLoader)
            throws PropertyTransformException {
        return createCollectionFactory(new HashSet<T>(), name, dataType, value, classLoader);
    }

    private <T> ObjectFactory<Collection<T>> createLinkedListFactory(String name, JavaGenericType dataType, Document value, ClassLoader classLoader)
            throws PropertyTransformException {
        return createCollectionFactory(new LinkedList<T>(), name, dataType, value, classLoader);
    }

    private <T> ObjectFactory<Collection<T>> createListFactory(String name, JavaGenericType dataType, Document value, ClassLoader classLoader)
            throws PropertyTransformException {
        return createCollectionFactory(new ArrayList<T>(), name, dataType, value, classLoader);
    }

    @SuppressWarnings({"unchecked"})
    private <T> ObjectFactory<Collection<T>> createCollectionFactory(Collection<T> collection,
                                                                     String name,
                                                                     JavaGenericType dataType,
                                                                     Document value,
                                                                     ClassLoader classLoader) throws PropertyTransformException {
        try {
            List<JavaTypeInfo> typeInfos = dataType.getLogical().getParameterTypesInfos();
            if (typeInfos.size() < 1) {
                // programming error
                throw new PropertyTransformException("List properties must have a value type");
            }
            Class<?> type = typeInfos.get(0).getRawType();

            List<Class<?>> types = new ArrayList<Class<?>>();
            types.add(type);

            Transformer<Node, ?> transformer = getTransformer(name, PROPERTY_TYPE, new JavaClass(type), types);

            Element root = value.getDocumentElement();
            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i).getFirstChild();
                T val = (T) transformer.transform(node, classLoader);
                collection.add(val);
            }
            return new SingletonObjectFactory<Collection<T>>(collection);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T> Transformer<T, ?> getTransformer(String name, DataType<?> sourceType, DataType<?> targetType, List<Class<?>> types)
            throws TransformationException, PropertyTransformException {
        Transformer<T, ?> transformer = (Transformer<T, ?>) transformerRegistry.getTransformer(sourceType, targetType, types, types);
        if (transformer == null) {
            throw new PropertyTransformException("No transformer for property " + name + " of type: " + targetType);
        }
        return transformer;
    }

}