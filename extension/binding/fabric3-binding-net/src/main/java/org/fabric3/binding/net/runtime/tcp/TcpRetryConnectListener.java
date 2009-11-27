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
package org.fabric3.binding.net.runtime.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.spi.binding.format.EncoderException;

/**
 * Listens for a channel connection event for a TCP socket, retrying a specified number of times if the operation failed.
 *
 * @version $Rev$ $Date$
 */
public class TcpRetryConnectListener implements ChannelFutureListener {
    private SocketAddress address;
    private byte[] serializedMessage;
    private ClientBootstrap bootstrap;
    private int maxRetry;
    private NetBindingMonitor monitor;
    private AtomicInteger retryCount;

    public TcpRetryConnectListener(byte[] serializedMessage,
                                   SocketAddress address,
                                   ClientBootstrap bootstrap,
                                   int maxRetry,
                                   NetBindingMonitor monitor) {
        this.serializedMessage = serializedMessage;
        this.address = address;
        this.bootstrap = bootstrap;
        this.maxRetry = maxRetry;
        this.monitor = monitor;
        retryCount = new AtomicInteger(0);
    }

    public void operationComplete(ChannelFuture future) throws Exception {
        Channel channel = future.getChannel();
        if (!future.isSuccess() && retryCount.getAndIncrement() >= maxRetry) {
            // connection failed and max number of retries exceeded
            monitor.error(future.getCause());
            return;
        }

        if (!future.isSuccess()) {
            // retry the connection
            ChannelFuture openFuture = bootstrap.connect(address);
            openFuture.addListener(this);
            return;
        }
        // connection succeeded, write data
        int size = serializedMessage.length;
        try {
            ChannelBufferFactory bufferFactory = channel.getConfig().getBufferFactory();
            ChannelBuffer dynamicBuffer = ChannelBuffers.dynamicBuffer(size, bufferFactory);
            ChannelBufferOutputStream bout = new ChannelBufferOutputStream(dynamicBuffer);
            // write the length of the stream
            bout.writeInt(size);
            // write contents to the buffer
            bout.write(serializedMessage);
            bout.flush();
            ChannelBuffer buffer = bout.buffer();
            // write to the channel
            ChannelFuture writeFuture = channel.write(buffer);
            // set the retry listener
            TcpRetryWriteListener listener = new TcpRetryWriteListener(buffer, maxRetry, monitor);
            writeFuture.addListener(listener);
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

}