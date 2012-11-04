/*
 * Fabric3 Copyright (c) 2009-2012 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.interceptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.spi.classloader.ClassLoaderObjectInputStream;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;

/**
 * Note this class will be removed when serialization with Kryo and Avro are in place.
 */
public abstract class AbstractMarshallingInterceptor implements Interceptor {
    protected ClassLoader loader;
    private Interceptor next;

    public AbstractMarshallingInterceptor(ClassLoader loader) {
        this.loader = loader;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }

    protected Message deserialize(Message message, ClassLoader loader) {
        ByteArrayInputStream bis = null;
        ObjectInputStream stream = null;
        try {
            Object body = message.getBody();
            if (body == null) {
                return message;
            }
            if (!(body instanceof byte[])) {
                throw new ServiceRuntimeException("Parameters must be a serialized byte array");
            }
            bis = new ByteArrayInputStream((byte[]) body);
            stream = new ClassLoaderObjectInputStream(bis, loader);
            message.setBody(stream.readObject());
            return message;
        } catch (IOException e) {
            throw new ServiceUnavailableException(e);
        } catch (ClassNotFoundException e) {
            throw new ServiceUnavailableException(e);
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

    protected Message serialize(Message message) {
        Object o = message.getBody();
        if (o == null) {
            return message;
        }
        if (!(o instanceof Serializable)) {
            throw new ServiceRuntimeException("Parameter must implement Serializable: " + o.getClass());
        }
        ObjectOutputStream stream = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            stream = new ObjectOutputStream(bos);
            stream.writeObject(o);
            stream.flush();
            message.setBody(bos.toByteArray());
            return message;
        } catch (IOException e) {
            throw new ServiceRuntimeException(e);
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
}
