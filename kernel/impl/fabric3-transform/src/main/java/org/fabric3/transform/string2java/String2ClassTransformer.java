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
 */
package org.fabric3.transform.string2java;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.TypeConstants;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.spi.transform.TransformationException;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class String2ClassTransformer implements SingleTypeTransformer<String, Class<?>> {
    private static final JavaType TARGET = new JavaType(Class.class);
    private ClassLoaderRegistry classLoaderRegistry;

    public String2ClassTransformer(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public DataType getSourceType() {
        return TypeConstants.STRING_TYPE;
    }

    public DataType getTargetType() {
        return TARGET;
    }

    public Class<?> transform(String source, ClassLoader loader) throws TransformationException {
        try {
            return classLoaderRegistry.loadClass(loader, source);
        } catch (ClassNotFoundException e) {
            throw new TransformationException(e);
        }
    }

}