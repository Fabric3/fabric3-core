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
package org.fabric3.spi.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.net.URI;

/**
 * A specialized ObjectInputStream that can deserialize objects loaded in different runtime classloaders. When ObjectInputStream.resolveClass() is
 * called, a classloader id is read if present in the byte stream. The id is used to resolve the classloader to load the class in.
 */
public class MultiClassLoaderObjectInputStream extends ObjectInputStream {
    private ClassLoaderRegistry registry;

    public MultiClassLoaderObjectInputStream(InputStream in, ClassLoaderRegistry registry) throws IOException {
        super(in);
        this.registry = registry;
    }

    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        int val = readByte();
        if (val == -1) {
            return super.resolveClass(desc);
        } else {
            byte[] bytes = new byte[val];
            int result = read(bytes);
            if (result == -1) {
                throw new IOException("Invalid classloader URL");
            }
            String id = new String(bytes);
            URI uri = URI.create(id);
            ClassLoader cl = registry.getClassLoader(uri);
            if (cl == null) {
                throw new IOException("Contribution not installed: " + id);
            }
            return Class.forName(desc.getName(), false, cl);
        }
    }

}
