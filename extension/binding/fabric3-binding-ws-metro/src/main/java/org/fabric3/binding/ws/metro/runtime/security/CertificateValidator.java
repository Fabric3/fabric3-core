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
package org.fabric3.binding.ws.metro.runtime.security;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import com.sun.xml.wss.impl.XWSSecurityRuntimeException;

/**
 * Validates certificates.
 */
public interface CertificateValidator {

    /**
     * Validates an X.509 certificate using a trust store.
     *
     * @param certificate the certificate to validate
     * @param trustStore  the trust store
     * @return true if valid
     * @throws XWSSecurityRuntimeException if a validation error occurs
     */
    boolean validate(X509Certificate certificate, KeyStore trustStore) throws XWSSecurityRuntimeException;

}
