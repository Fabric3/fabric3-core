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
package org.fabric3.spi.security;

import java.io.File;
import java.security.KeyStore;

/**
 * Manages configuring and loads key and trust stores.
 *
 * @version $Rev$ $Date$
 */
public interface KeyStoreManager {

    /**
     * Returns the loaded key store configured for the runtime or null if a key store is not available.
     *
     * @return the loaded key store or null
     */
    KeyStore getKeyStore();

    /**
     * Returns the key store location configured for the runtime or null if a key store is not available.
     *
     * @return the key store location or null
     */
    File getKeyStoreLocation();

    /**
     * Returns the key store password or null if a password is not configured.
     *
     * @return the key store password or null if a password is not configured.
     */
    String getKeyStorePassword();

    /**
     * Returns the loaded trust store configured for the runtime or null if a key store is not available.
     *
     * @return the loaded trust store or null
     */
    KeyStore getTrustStore();

    /**
     * Returns the trust store password or null if a password is not configured.
     *
     * @return the trust store password or null if a password is not configured.
     */
    String getTrustStorePassword();

    /**
     * Returns the trust store location configured for the runtime or null if a key store is not available.
     *
     * @return the trust store location or null
     */
    File getTrustStoreLocation();

    /**
     * Returns the default cert password or null if a password is not configured
     *
     * @return the default cert password or null
     */
    String getCertPassword();

}
