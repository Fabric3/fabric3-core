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
package org.fabric3.api;

import java.io.File;
import java.net.URI;

import org.oasisopen.sca.ComponentContext;

/**
 * A Fabric3 extension to the OASIS SCA ComponentContext API. Components may reference this interface when for fields or setters marked with @Context instead of
 * the SCA RequestContext variant. For example:
 * <pre>
 * public class SomeComponent implements SomeService {
 *      &#064;Context
 *      protected Fabric3ComponentContext context;
 *      //...
 * }
 * </pre>
 * At runtime, the <code>context</code> field will be injected with an instance of Fabric3ComponentContext.
 */
public interface Fabric3ComponentContext extends ComponentContext {

    /**
     * Returns the unique name associated with this runtime. Names survive restarts.
     *
     * @return the unique runtime name
     */
    String getRuntimeName();

    /**
     * Returns the SCA domain associated with this runtime.
     *
     * @return the SCA domain associated with this runtime
     */
    URI getDomain();

    /**
     * Returns the runtime environment type.
     *
     * @return the runtime environment type
     */
    String getEnvironment();

    /**
     * Returns the directory where persistent data can be written.
     *
     * @return the directory where persistent data can be written or null if the runtime does not support persistent capabilities
     */
    File getDataDirectory();

    /**
     * Returns the temporary directory.
     *
     * @return the temporary directory.
     */
    File getTempDirectory();

}
