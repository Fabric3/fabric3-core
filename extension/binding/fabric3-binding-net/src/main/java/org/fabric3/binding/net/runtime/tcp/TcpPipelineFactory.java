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
package org.fabric3.binding.net.runtime.tcp;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.handler.timeout.Timer;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Creates a TCP channel pipeline for both clients and service providers.
 *
 * @version $Rev$ $Date$
 */
public class TcpPipelineFactory implements ChannelPipelineFactory {
    private final ChannelHandler handler;
    private Timer timer;
    private long timeout;

    public TcpPipelineFactory(ChannelHandler handler, Timer timer, long timeout) {
        this.handler = handler;
        this.timer = timer;
        this.timeout = timeout;
    }

    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();
        int secondsTimeout = (int) (timeout / 10);
        pipeline.addLast("idlehandler", new IdleStateHandler(timer, secondsTimeout, secondsTimeout, secondsTimeout));
        pipeline.addLast("readTimeout", new ReadTimeoutHandler(timer, timeout, TimeUnit.MILLISECONDS));
        pipeline.addLast("writeTimeout", new WriteTimeoutHandler(timer, timeout, TimeUnit.MILLISECONDS));
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        pipeline.addLast("handler", handler);
        return pipeline;
    }
}