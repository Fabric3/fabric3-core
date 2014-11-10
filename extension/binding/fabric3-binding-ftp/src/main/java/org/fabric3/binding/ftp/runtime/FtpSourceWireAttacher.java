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

import java.net.URI;

import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.ftp.provision.FtpWireSourceDefinition;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.transport.ftp.spi.FtpLetContainer;

/**
 *
 */
public class FtpSourceWireAttacher implements SourceWireAttacher<FtpWireSourceDefinition> {

    private FtpLetContainer ftpLetContainer;
    private BindingMonitor monitor;

    /**
     * Injects the references.
     *
     * @param ftpLetContainer FtpLet container.  The FtpLet container is optional. If it is not available, only reference bindings will be supported.
     * @param monitor         the binding monitor for reporting events.
     */
    public FtpSourceWireAttacher(@Reference(required = false) FtpLetContainer ftpLetContainer, @Monitor BindingMonitor monitor) {
        this.ftpLetContainer = ftpLetContainer;
        this.monitor = monitor;
    }

    public void attach(FtpWireSourceDefinition source, PhysicalWireTargetDefinition target, final Wire wire) throws ContainerException {
        URI uri = source.getUri();
        String servicePath = uri.getSchemeSpecificPart();
        if (servicePath.startsWith("//")) {
            servicePath = servicePath.substring(2);
        }
        BindingFtpLet bindingFtpLet = new BindingFtpLet(servicePath, wire, monitor);
        if (ftpLetContainer == null) {
            throw new ContainerException(
                    "An FTP server was not configured for this runtime. Ensure the FTP server extension is installed and configured properly.");
        }
        ftpLetContainer.registerFtpLet(servicePath, bindingFtpLet);

    }

    public void detach(FtpWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(FtpWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
        throw new UnsupportedOperationException();
    }

    public void attachObjectFactory(FtpWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition definition)
            throws ContainerException {
        throw new UnsupportedOperationException();
    }


}
