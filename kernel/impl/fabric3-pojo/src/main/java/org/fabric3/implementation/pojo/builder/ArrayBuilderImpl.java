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

package org.fabric3.implementation.pojo.builder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.ParamTypes;

import static org.fabric3.spi.model.type.xsd.XSDConstants.PROPERTY_TYPE;

/**
 *
 */
public class ArrayBuilderImpl extends AbstractPropertyBuilder implements ArrayBuilder {

    public ArrayBuilderImpl(@Reference TransformerRegistry registry) {
        super(registry);
    }


    @SuppressWarnings({"unchecked"})
    public ObjectFactory<?> createFactory(String name, DataType dataType, Document value, ClassLoader classLoader) throws PropertyTransformException {
        try {
            Class componentType = dataType.getType().getComponentType();
            Class<?> type = componentType;
            if (type.isPrimitive()) {
                type = ParamTypes.PRIMITIVE_TO_OBJECT.get(type);
            }

            List<Class<?>> types = new ArrayList<>();
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
            return new SingletonObjectFactory<>(array);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, e);
        }
    }

}