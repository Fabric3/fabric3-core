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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.executor;

import java.net.URI;

import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.command.AttachExtensionCommand;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * Executes AttachExtensionCommands.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class AttachExtensionCommandExecutor implements CommandExecutor<AttachExtensionCommand> {

    private HostInfo info;
    private CommandExecutorRegistry commandExecutorRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    @Constructor
    public AttachExtensionCommandExecutor(@Reference HostInfo info,
                                          @Reference CommandExecutorRegistry commandExecutorRegistry,
                                          @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.info = info;
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(AttachExtensionCommand.class, this);
    }

    public void execute(AttachExtensionCommand command) throws ExecutionException {
        if (!info.supportsClassLoaderIsolation()) {
            return;
        }
        URI contributionUri = command.getContribution();
        URI providerUri = command.getProvider();
        // note: casts are safe as all extension and provider classloaders are multi-parent
        MultiParentClassLoader contributionCl = (MultiParentClassLoader) classLoaderRegistry.getClassLoader(contributionUri);
        MultiParentClassLoader providerCl = (MultiParentClassLoader) classLoaderRegistry.getClassLoader(providerUri);
        contributionCl.addExtensionClassLoader(providerCl);
    }
}