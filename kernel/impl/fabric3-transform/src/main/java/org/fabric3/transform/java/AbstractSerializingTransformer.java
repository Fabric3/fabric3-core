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
package org.fabric3.transform.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.classloader.ClassLoaderObjectInputStream;
import org.fabric3.spi.transform.Transformer;

/**
 * Base case for transforming data using Java serialization.
 */
public abstract class AbstractSerializingTransformer<S, T> implements Transformer<S, T> {

    protected byte[] serialize(Object o) throws Fabric3Exception {
        if (o == null) {
            throw new IllegalArgumentException("Attempt to serialize a null object");
        }
        if (!(o instanceof Serializable)) {
            throw new IllegalArgumentException("Parameters for Java-to-Java transformations must implement Serializable: " + o.getClass());
        }
        ObjectOutputStream stream = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            stream = new ObjectOutputStream(bos);
            stream.writeObject(o);
            stream.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected Serializable deserialize(byte[] bytes, ClassLoader loader) throws Fabric3Exception {
        ByteArrayInputStream bis = null;
        ObjectInputStream stream = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            stream = new ClassLoaderObjectInputStream(bis, loader);
            return (Serializable) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new Fabric3Exception(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}