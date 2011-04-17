/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.binding.zeromq.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * @version $Revision$ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar
 *          2011) $
 * 
 */
public class ZeroMQEventStreamHandler implements EventStreamHandler {

    private ClassLoader          classloader;
    private ZMQMessagePublisher publisher;

    public ZeroMQEventStreamHandler(ZMQMessagePublisher publisher, ClassLoader loader) {
        this.publisher = publisher;
        this.classloader = loader;
    }

    @Override
    public void handle(Object event) {
        if (!(event instanceof Serializable)) {
            throw new ServiceRuntimeException("Event type must be serializable: " + event.getClass().getName());
        }

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classloader);

        Serializable serializable = (Serializable) event;
        // TODO add pluggable serializers
        byte[] data = getByteArray(event);

        publisher.sendMessage(data);
        Thread.currentThread().setContextClassLoader(oldLoader);
    }

    private byte[] getByteArray(Object event) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] ret = null;
        try {
            out = new MultiClassLoaderObjectOutputStream(bos);
            out.writeObject(event);
            out.flush();
            out.close();
            bos.flush();
            ret = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                out.close();
                bos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ret;

    }

    @Override
    public void setNext(EventStreamHandler next) {
    }

    @Override
    public EventStreamHandler getNext() {
        return null;
    }

}
