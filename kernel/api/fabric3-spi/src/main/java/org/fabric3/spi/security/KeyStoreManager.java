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
 */
package org.fabric3.spi.security;

import java.io.File;
import java.security.KeyStore;

/**
 * Manages configuring and loads key and trust stores.
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
