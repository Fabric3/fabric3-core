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
package org.fabric3.binding.net.runtime.http;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpRequest;

import org.fabric3.binding.net.NetBindingMonitor;

/**
 * Listens for the completion of an HTTP channel write operation and retries the specified number of times if the operation failed.
 *
 * @version $Rev$ $Date$
 */
public class HttpRetryWriteListener implements ChannelFutureListener {
    private HttpRequest request;
    private NetBindingMonitor monitor;
    private int maxRetry;
    private AtomicInteger retryCount;

    /**
     * Constructor.
     *
     * @param request  the HTTP request being written
     * @param maxRetry the maximum number of times to retry on failure
     * @param monitor  the communications monitor
     */
    public HttpRetryWriteListener(HttpRequest request, int maxRetry, NetBindingMonitor monitor) {
        this.request = request;
        this.monitor = monitor;
        this.maxRetry = maxRetry;
        retryCount = new AtomicInteger(0);
    }

    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            future.getChannel().close();
            return;
        }
        if (!future.isSuccess() && retryCount.getAndIncrement() >= maxRetry) {
            // failed and maximum number of retries exceeded
            monitor.error(future.getCause());
            return;
        }
        // retry the write request
        ChannelFuture writeFuture = future.getChannel().write(request);
        writeFuture.addListener(this);
    }
}
