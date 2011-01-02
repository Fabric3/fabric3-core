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
package org.fabric3.binding.net.runtime.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.timeout.HashedWheelTimer;
import org.jboss.netty.handler.timeout.Timer;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.net.NetBindingMonitor;
import org.fabric3.binding.net.runtime.TransportService;
import org.fabric3.binding.net.runtime.WireHolder;
import org.fabric3.binding.net.runtime.http.HttpRequestHandler;
import org.fabric3.binding.net.runtime.http.HttpServerPipelineFactory;
import org.fabric3.binding.net.runtime.tcp.TcpPipelineFactory;
import org.fabric3.binding.net.runtime.tcp.TcpRequestHandler;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.builder.WiringException;

/**
 * @version $Rev$ $Date$
 */
public class TransportServiceImpl implements TransportService {
    private final ExecutorService executorService;
    private NetBindingMonitor monitor;
    private Map<String, MessageEncoder> messageFormatters = new HashMap<String, MessageEncoder>();

    private long connectTimeout = 10000;
    private String ipAddress = "127.0.0.1";

    private int httpPort = 8282;
    private int httpsPort = 8484;
    private int tcpPort = 8383;
    private long maxObjectSize = 2000000;
    private String tcpMessageFormat = "jdk.wrapped";
    // FIXME HTTP message format should be part of WireHolder and created in source attacher, not here. However, for tcp, it is fixed here
    private String httpMessageFormat = "jdk";

    private Timer timer;
    private ChannelFactory factory;
    private Channel httpChannel;
    private Channel tcpChannel;
    private HttpRequestHandler httpRequestHandler;
    private TcpRequestHandler tcpRequestHandler;

    public TransportServiceImpl(@Reference ExecutorService executorService, @Monitor NetBindingMonitor monitor) {
        this.executorService = executorService;
        this.monitor = monitor;
    }

    @Reference(required = false)
    public void setMessageFormatters(Map<String, MessageEncoder> messageFormatters) {
        this.messageFormatters = messageFormatters;
    }

    @Property(required = false)
    public void setConnectTimeout(long timeout) {
        this.connectTimeout = timeout;
    }

    @Property(required = false)
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Property(required = false)
    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    @Property(required = false)
    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    @Property(required = false)
    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    @Property(required = false)
    public void setMaxObjectSize(long maxObjectSize) {
        this.maxObjectSize = maxObjectSize;
    }

    @Property(required = false)
    public void setTcpMessageFormat(String tcpMessageFormat) {
        this.tcpMessageFormat = tcpMessageFormat;
    }

    @Property(required = false)
    public void setHttpMessageFormat(String httpMessageFormat) {
        this.httpMessageFormat = httpMessageFormat;
    }

    @Init
    public void init() {
        factory = new NioServerSocketChannelFactory(executorService, executorService);
        timer = new HashedWheelTimer();
    }

    @Destroy
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public void destroy() {
        if (httpChannel != null) {
            ChannelFuture future = httpChannel.close();
            future.awaitUninterruptibly();
            if (!future.isSuccess()) {
                monitor.error(future.getCause());
            }
            // Don't release resources as it waits for the core thread pool to cease operations
            // factory.releaseExternalResources();
        }
        if (tcpChannel != null) {
            ChannelFuture future = tcpChannel.close();
            future.awaitUninterruptibly();
            if (!future.isSuccess()) {
                monitor.error(future.getCause());
            }
        }
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void registerHttp(String path, WireHolder wireHolder) throws WiringException {
        if (httpRequestHandler == null) {
            createHttpChannel();
        }
        httpRequestHandler.register(path, wireHolder);
        monitor.httpEndpointProvisioned(ipAddress, httpPort, path);
    }

    public void registerTcp(String path, WireHolder wireHolder) throws WiringException {
        if (tcpRequestHandler == null) {
            createTcpChannel();
        }
        tcpRequestHandler.register(path, wireHolder);
        monitor.tcpEndpointProvisioned(ipAddress, tcpPort, path);
    }

    public void unregisterHttp(String path) {
        httpRequestHandler.unregister(path);
        monitor.httpEndpointRemoved(ipAddress, httpPort, path);
    }

    public void unregisterTcp(String path) {
        tcpRequestHandler.unregister(path);
        monitor.tcpEndpointRemoved(ipAddress, tcpPort, path);
    }

    private void createHttpChannel() throws WiringException {
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        MessageEncoder messageEncoder = messageFormatters.get(httpMessageFormat);
        if (messageEncoder == null) {
            throw new WiringException("Message formatter not found:" + httpMessageFormat);
        }
        httpRequestHandler = new HttpRequestHandler(messageEncoder, monitor);
        HttpServerPipelineFactory pipeline = new HttpServerPipelineFactory(httpRequestHandler, timer, connectTimeout);
        bootstrap.setPipelineFactory(pipeline);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // TODO  configure following integer vals:
        // receiveBufferSize, sendBufferSize, writeBufferHighWaterMark, writeBufferLowWaterMark, writeSpinCount, receiveBufferSizePredictor

        // Bind and start to accept incoming connections.
        InetSocketAddress socketAddress = new InetSocketAddress(ipAddress, httpPort);
        httpChannel = bootstrap.bind(socketAddress);
        monitor.startHttpListener(httpPort);
    }

    private void createTcpChannel() throws WiringException {
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        MessageEncoder messageEncoder = messageFormatters.get(tcpMessageFormat);
        if (messageEncoder == null) {
            throw new WiringException("Message formatter not found:" + tcpMessageFormat);
        }
        tcpRequestHandler = new TcpRequestHandler(messageEncoder, maxObjectSize, monitor);
        TcpPipelineFactory pipeline = new TcpPipelineFactory(tcpRequestHandler, timer, connectTimeout);
        bootstrap.setPipelineFactory(pipeline);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // TODO  configure following integer vals:
        // receiveBufferSize, sendBufferSize, writeBufferHighWaterMark, writeBufferLowWaterMark, writeSpinCount, receiveBufferSizePredictor

        // Bind and start to accept incoming connections.
        InetSocketAddress socketAddress = new InetSocketAddress(ipAddress, tcpPort);
        tcpChannel = bootstrap.bind(socketAddress);
        monitor.startTcpListener(tcpPort);
    }


}
