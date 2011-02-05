/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.management.rest.runtime;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * @version $Rev$ $Date$
 */
public class TransformerPairServiceImpl implements TransformerPairService {
    private static final JavaClass<?> JAVA_TYPE = new JavaClass<Object>(Object.class);

    private TransformerRegistry registry;

    public TransformerPairServiceImpl(@Reference TransformerRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings({"unchecked"})
    public TransformerPair getTransformerPair(List<Method> methods, DataType<?> inputType, DataType<?> outputType) throws TransformationException {
        List<Class<?>> list = new ArrayList<Class<?>>();
        for (Method method : methods) {
            list.addAll(Arrays.asList(method.getParameterTypes()));
            list.addAll(Arrays.asList(method.getExceptionTypes()));
            list.add(method.getReturnType());
        }
        Transformer<InputStream, Object> deserializer =
                (Transformer<InputStream, Object>) registry.getTransformer(inputType, JAVA_TYPE, list, list);
        Transformer<Object, byte[]> serializer = (Transformer<Object, byte[]>) registry.getTransformer(JAVA_TYPE, outputType, list, list);
        return new TransformerPair(deserializer, serializer);
    }

}
