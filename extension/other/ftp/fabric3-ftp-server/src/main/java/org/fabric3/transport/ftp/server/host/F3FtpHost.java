/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.transport.ftp.server.host;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;

/**
 * F3 implementation of the in-process FTP host.
 */
@EagerInit
public class F3FtpHost implements FtpHost {
    private FtpHostMonitor monitor;
    private ExecutorService executorService;
    private int commandPort = 2000;
    private SocketAcceptor acceptor;
    private IoHandler ftpHandler;
    private ProtocolCodecFactory codecFactory;
    private String listenAddress;
    private int idleTimeout = 60; // 60 seconds default

    /**
     * Starts the FTP server.
     *
     * @throws IOException If unable to start the FTP server.
     */
    @Init
    public void start() throws IOException {
        InetSocketAddress socketAddress;
        if (listenAddress == null) {
            socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), commandPort);
        } else {
            socketAddress = new InetSocketAddress(listenAddress, commandPort);
        }
        acceptor = new NioSocketAcceptor();
        SocketSessionConfig config = acceptor.getSessionConfig();
        config.setIdleTime(IdleStatus.BOTH_IDLE, idleTimeout);
        acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(executorService));
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
        acceptor.setHandler(ftpHandler);
        acceptor.bind(socketAddress);
        monitor.startFtpListener(commandPort);
    }

    /**
     * Stops the FTP server.
     */
    @Destroy
    public void stop() {
        acceptor.unbind();
        acceptor.dispose();
    }

    /**
     * Sets the monitor.
     *
     * @param monitor the monitor.
     */
    @Monitor
    public void setMonitor(FtpHostMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Sets the handler for the FTP commands.
     *
     * @param ftpHandler FTP Handler.
     */
    @Reference
    public void setFtpHandler(IoHandler ftpHandler) {
        this.ftpHandler = ftpHandler;
    }

    /**
     * Sets the protocol codec factory.
     *
     * @param codecFactory Protocol codec.
     */
    @Reference
    public void setCodecFactory(ProtocolCodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }

    /**
     * Sets the work scheduler for task execution.
     *
     * @param executorService the scheduler
     */
    @Reference
    public void setWorkScheduler(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Sets the FTP command port.
     *
     * @param commandPort Command port.
     */
    @Property(required = false)
    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }

    /**
     * Sets the optional timeout in milliseconds for sockets that are idle.
     *
     * @param timeout timeout in milliseconds.
     */
    @Property(required = false)
    public void setIdleTimeout(int timeout) {
        this.idleTimeout = timeout / 1000;   // convert to seconds used by Mina
    }

    /**
     * Sets the address the server should bind to. This is used for multi-homed machines.
     *
     * @param listenAddress the address to bind to
     */
    @Property(required = false)
    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }
}
