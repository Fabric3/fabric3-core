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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.runtime.bootstrap;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import org.fabric3.host.runtime.ComponentRegistration;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.InitializationException;

/**
 * Bootstraps a runtime in two phases. The first phase initializes the runtime domain. The second phase initializes the core runtime services.
 *
 * @version $Rev$ $Date$
 */
public interface Bootstrapper {
    /**
     * Initializes the domain for the given runtime.
     *
     * @param runtime            the runtime to initialize the domain for
     * @param systemCompositeUrl the URL of the system composite file
     * @param systemConfigSource the source for the system configuration property
     * @param hostClassLoader    the host classloader that is shared between application classes and runtime classes
     * @param bootClassLoader    the bootstrap classloader
     * @param registrations      extension components provided by the host runtime
     * @param exportedPackages   the Java packages exported by the boot contribution
     * @throws InitializationException if there was a problem bootstrapping the runtime
     */
    public void bootRuntimeDomain(Fabric3Runtime runtime,
                                  URL systemCompositeUrl,
                                  Document systemConfigSource,
                                  ClassLoader hostClassLoader,
                                  ClassLoader bootClassLoader,
                                  List<ComponentRegistration> registrations,
                                  Map<String, String> exportedPackages) throws InitializationException;

    /**
     * Initialize the core system components for the supplied runtime.
     *
     * @throws InitializationException if there was a problem bootstrapping the runtime
     */
    public void bootSystem() throws InitializationException;

}
