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
package org.fabric3.binding.ftp.runtime;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.net.SocketFactory;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.binding.ftp.provision.FtpWireTargetDefinition;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;

/**
 *
 */
public class FtpTargetWireAttacher implements TargetWireAttacher<FtpWireTargetDefinition> {
    private FtpInterceptorMonitor monitor;

    public FtpTargetWireAttacher(@Monitor FtpInterceptorMonitor monitor) {
        this.monitor = monitor;
    }

    public void attach(PhysicalWireSourceDefinition source, FtpWireTargetDefinition target, Wire wire) throws ContainerException {

        InvocationChain invocationChain = wire.getInvocationChains().iterator().next();
        URI uri = target.getUri();
        try {
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 23 : uri.getPort();
            InetAddress hostAddress = "localhost".equals(host) ? InetAddress.getLocalHost() : InetAddress.getByName(host);

            String remotePath = uri.getPath();
            String tmpFileSuffix = target.getTmpFileSuffix();

            FtpSecurity security = target.getSecurity();
            boolean active = target.isActive();
            int connectTimeout = target.getConectTimeout();
            SocketFactory factory = new ExpiringSocketFactory(connectTimeout);
            int socketTimeout = target.getSocketTimeout();
            List<String> cmds = target.getSTORCommands();
            FtpTargetInterceptor targetInterceptor =
                    new FtpTargetInterceptor(hostAddress, port, security, active, socketTimeout, factory, cmds, monitor);
            targetInterceptor.setTmpFileSuffix(tmpFileSuffix);
            targetInterceptor.setRemotePath(remotePath);

            invocationChain.addInterceptor(targetInterceptor);
        } catch (UnknownHostException e) {
            throw new ContainerException(e);
        }

    }

    public void detach(PhysicalWireSourceDefinition source, FtpWireTargetDefinition target) throws ContainerException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(FtpWireTargetDefinition target) throws ContainerException {
        throw new AssertionError();
    }


}
