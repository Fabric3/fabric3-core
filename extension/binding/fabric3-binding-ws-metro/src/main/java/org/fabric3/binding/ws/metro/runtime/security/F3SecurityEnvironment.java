/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 * Originally based on code from the Sun Metro Project XWS-Security extension:
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
*/
package org.fabric3.binding.ws.metro.runtime.security;

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import com.sun.org.apache.xml.internal.security.utils.RFC2253Parser;
import com.sun.xml.ws.security.impl.kerberos.KerberosContext;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.XWSSConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.core.Timestamp;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.saml.Assertion;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSName;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.api.SecuritySubject;
import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.KeyStoreManager;
import org.fabric3.spi.security.UsernamePasswordToken;

/**
 * Partially implements the Metro security SPI for hosting runtimes. SAML and Kerberos operations are not supported.
 */
public class F3SecurityEnvironment implements SecurityEnvironment {
    private static final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    private AuthenticationService authenticationService;
    private KeyStoreManager keyStoreManager;
    private CertificateValidator certificateValidator;
    private KeyStore keyStore;
    private KeyStore trustStore;

    private char[] keyStorePassword;

    private final SimpleDateFormat calendarFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final SimpleDateFormat calendarFormatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");

    public F3SecurityEnvironment(@Reference AuthenticationService authenticationService,
                                 @Reference CertificateValidator certificateValidator,
                                 @Reference KeyStoreManager keyStoreManager) {
        this.authenticationService = authenticationService;
        this.certificateValidator = certificateValidator;
        this.keyStoreManager = keyStoreManager;
    }


    @Init
    public void init() throws XWSSecurityException {
        keyStore = keyStoreManager.getKeyStore();
        String password = keyStoreManager.getKeyStorePassword();
        if (password != null) {
            keyStorePassword = password.toCharArray();
        }
        trustStore = keyStoreManager.getTrustStore();
    }

    public X509Certificate getDefaultCertificate(Map context) throws XWSSecurityRuntimeException {
        checkEnabled();
        return getDefaultCertificateInternal(trustStore, context);
    }

    public X509Certificate getCertificate(Map context, String alias, boolean forSigning) throws XWSSecurityRuntimeException {
        checkEnabled();
        try {
            if (forSigning) {
                if (((alias == null) || ("".equals(alias)) && forSigning)) {
                    return getDefaultCertificate(context);
                }
                return (X509Certificate) keyStore.getCertificate(alias);
            } else {
                if ("".equals(alias) || (alias == null)) {
                    return getDefaultCertificateInternal(trustStore, context);
                } else {
                    return (X509Certificate) trustStore.getCertificate(alias);
                }
            }
        } catch (KeyStoreException e) {
            throw new XWSSecurityRuntimeException(e);
        }
    }

    public SecretKey getSecretKey(Map context, String alias, boolean encryptMode) throws XWSSecurityException {
        checkEnabled();
        throw new UnsupportedOperationException();
    }

    public PrivateKey getPrivateKey(Map context, String alias) throws XWSSecurityRuntimeException {
        checkEnabled();
        try {
            return (PrivateKey) keyStore.getKey(alias, keyStorePassword);
        } catch (KeyStoreException e) {
            throw new XWSSecurityRuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new XWSSecurityRuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            throw new XWSSecurityRuntimeException(e);
        }
    }

    public PublicKey getPublicKey(Map context, byte[] identifier) throws XWSSecurityRuntimeException, XWSSecurityException {
        checkEnabled();
        return getCertificate(context, identifier).getPublicKey();
    }

    public PublicKey getPublicKey(Map context, byte[] identifier, String valueType) throws XWSSecurityRuntimeException, XWSSecurityException {
        checkEnabled();
        if (MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)) {
            return getCertificate(context, identifier).getPublicKey();
        }
        throw new UnsupportedOperationException();
    }

    public X509Certificate getCertificate(Map context, byte[] identifier) throws XWSSecurityRuntimeException, XWSSecurityException {
        checkEnabled();
        try {
            if (trustStore != null) {
                Enumeration aliases = trustStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    Certificate cert = trustStore.getCertificate(alias);
                    if (cert == null || !"X.509".equals(cert.getType())) {
                        continue;
                    }
                    X509Certificate x509Cert = (X509Certificate) cert;
                    byte[] keyId = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(x509Cert);
                    if (keyId == null) {
                        // Cert does not contain a key identifier
                        continue;
                    }
                    if (Arrays.equals(identifier, keyId)) {
                        return x509Cert;
                    }
                }
            }
        } catch (KeyStoreException e) {
            throw new XWSSecurityRuntimeException(e);
        }
        throw new XWSSecurityRuntimeException("Certificate not found");
    }

    public X509Certificate getCertificate(Map context, byte[] identifier, String valueType) throws XWSSecurityException {
        checkEnabled();
        if (MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)) {
            return getCertificate(context, identifier);
        }
        throw new UnsupportedOperationException();
    }

    public PrivateKey getPrivateKey(Map context, X509Certificate certificate) throws XWSSecurityRuntimeException {
        checkEnabled();
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert != null && cert.equals(certificate)) {
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword);
                }
            }
        } catch (KeyStoreException e) {
            throw new XWSSecurityRuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new XWSSecurityRuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            throw new XWSSecurityRuntimeException(e);
        }
        throw new XWSSecurityRuntimeException("Private key not found");
    }

    public PrivateKey getPrivateKey(Map context, BigInteger serialNumber, String issuerName) throws XWSSecurityRuntimeException {
        checkEnabled();
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName = RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName) && thisSerialNumber.equals(serialNumber)) {
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword);
                }
            }
        } catch (Exception e) {
            throw new XWSSecurityRuntimeException(e);
        }
        throw new XWSSecurityRuntimeException("Private key not found for serial number: " + serialNumber);
    }

    public X509Certificate getCertificate(Map context, PublicKey publicKey, boolean forSign) throws XWSSecurityRuntimeException {
        checkEnabled();
        try {
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                if (x509Cert.getPublicKey().equals(publicKey)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new XWSSecurityRuntimeException(e);
        }
        throw new XWSSecurityRuntimeException("Certificate not found");
    }

    public PrivateKey getPrivateKey(Map context, byte[] identifier) throws XWSSecurityRuntimeException, XWSSecurityException {
        checkEnabled();
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // certificate does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(identifier, keyId)) {
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new XWSSecurityRuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            throw new XWSSecurityRuntimeException(e);
        } catch (KeyStoreException e) {
            throw new XWSSecurityRuntimeException(e);
        }
        throw new XWSSecurityRuntimeException("Private key not found");
    }

    public PrivateKey getPrivateKey(Map context, byte[] identifier, String type) throws XWSSecurityException {
        checkEnabled();
        if (MessageConstants.KEY_INDETIFIER_TYPE.equals(type)) {
            return getPrivateKey(context, identifier);
        }
        throw new UnsupportedOperationException();
    }

    public PrivateKey getPrivateKey(Map context, PublicKey publicKey, boolean forSign) throws XWSSecurityRuntimeException {
        checkEnabled();
        if (forSign) {
            throw new UnsupportedOperationException();
        } else {
            try {
                Enumeration aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    if (keyStore.isKeyEntry(alias)) {
                        Certificate cert = keyStore.getCertificate(alias);
                        if (publicKey.equals(cert.getPublicKey())) {
                            return (PrivateKey) keyStore.getKey(alias, keyStorePassword);
                        }
                    }
                }
            } catch (KeyStoreException e) {
                throw new XWSSecurityRuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new XWSSecurityRuntimeException(e);
            } catch (UnrecoverableKeyException e) {
                throw new XWSSecurityRuntimeException(e);
            }
        }
        throw new XWSSecurityRuntimeException("Private key not found");
    }

    public PublicKey getPublicKey(Map context, BigInteger serialNumber, String issuerName) throws XWSSecurityRuntimeException {
        checkEnabled();
        return getCertificate(context, serialNumber, issuerName).getPublicKey();
    }

    public X509Certificate getCertificate(Map context, BigInteger serialNumber, String issuerName) throws XWSSecurityRuntimeException {
        checkEnabled();
        try {
            if (trustStore != null) {
                Enumeration aliases = trustStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    Certificate cert = trustStore.getCertificate(alias);
                    if (cert == null || !"X.509".equals(cert.getType())) {
                        continue;
                    }
                    X509Certificate x509Cert = (X509Certificate) cert;
                    String thisIssuerName = RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                    BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                    if (thisIssuerName.equals(issuerName) && thisSerialNumber.equals(serialNumber)) {
                        return x509Cert;
                    }
                }
            }
        } catch (KeyStoreException e) {
            throw new XWSSecurityRuntimeException(e);
        }
        throw new XWSSecurityRuntimeException("Certificate key not found");
    }

    public boolean authenticateUser(Map context, String username, String password) throws XWSSecurityRuntimeException {
        WorkContext workContext = (WorkContext) context.get(MetroConstants.WORK_CONTEXT);
        if (workContext == null) {
            // programming error
            throw new AssertionError("Work context not set");
        }
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        try {
            SecuritySubject subject = authenticationService.authenticate(token);
            workContext.setSubject(subject);
            return true;
        } catch (AuthenticationException e) {
            throw new XWSSecurityRuntimeException(e);
        }
    }

    public boolean authenticateUser(Map context, String username, String digest, String nonce, String created) throws XWSSecurityException {
        throw new UnsupportedOperationException("Digest authentication not supported");
    }

    public String getUsername(Map context) {
        // username is configured as part of the binding in a composite. It is set using the BindingProvider API in
        // {@link org.fabric3.binding.ws.metro.runtime.core.MetroTargetInterceptor.}
        return (String) context.get(MetroConstants.USERNAME);
    }

    public String getPassword(Map context) {
        // password is configured as part of the binding in a composite. It is set using the BindingProvider API in
        // {@link org.fabric3.binding.ws.metro.runtime.core.MetroTargetInterceptor.}
        return (String) context.get(MetroConstants.PASSWORD);
    }

    public String authenticateUser(Map context, String username) {
        throw new UnsupportedOperationException("Username + context authentication not supported");
    }

    public Subject getSubject() {
        throw new UnsupportedOperationException();
    }

    public void validateTimestamp(Map context, Timestamp timestamp, long maxClockSkew, long freshnessLimit) {
        checkEnabled();
        validateTimestamp(context, timestamp.getCreated(), timestamp.getExpires(), maxClockSkew, freshnessLimit);
    }


    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void validateTimestamp(Map context, String created, String expires, long maxClockSkew, long freshnessLimit) {
        checkEnabled();
        if (expiresBeforeCreated(created, expires)) {
            XWSSecurityRuntimeException e = new XWSSecurityRuntimeException("Message expired!");
            QName name = new QName(WSU_NS, "MessageExpired", "wsu");
            WssSoapFaultException sfe = new WssSoapFaultException(name, "Message expired", null, null);
            sfe.initCause(e);
            throw sfe;
        }
        validateCreationTime(context, created, maxClockSkew, freshnessLimit);
    }


    public void validateCreationTime(Map context, String creationTime, long maxClockSkew, long timestampFreshnessLimit)
            throws XWSSecurityRuntimeException {
        checkEnabled();
        SimpleDateFormat calendarFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat calendarFormatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
        Date created;

        try {
            created = calendarFormatter1.parse(creationTime);
        } catch (java.text.ParseException pe) {
            try {
                created = calendarFormatter2.parse(creationTime);
            } catch (java.text.ParseException e) {
                throw new XWSSecurityRuntimeException(e);
            }
        }

        Date current = getFreshnessAndSkewAdjustedDate(maxClockSkew, timestampFreshnessLimit);

        if (created.before(current)) {
            throw new XWSSecurityRuntimeException("The creation time is older than the current time");
        }

        Date currentTime = getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, true);
        if (currentTime.before(created)) {
            throw new XWSSecurityRuntimeException("The creation time is ahead of the current time.");
        }
    }

    public boolean validateCertificate(X509Certificate certificate, Map context) throws XWSSecurityRuntimeException {
        checkEnabled();
        return certificateValidator.validate(certificate, keyStore);
    }

    public CallbackHandler getCallbackHandler() {
        return null;
    }

    public void updateOtherPartySubject(Subject subject, String username, String password) {
        // no-op
    }

    public void updateOtherPartySubject(Subject subject, X509Certificate certificate) {
        // no-op
    }

    public void updateOtherPartySubject(Subject subject, Assertion assertion) {
        // no-op
    }

    public void updateOtherPartySubject(Subject subject, XMLStreamReader reader) {
        // no-op
    }

    public void updateOtherPartySubject(Subject subject, Subject bootstrap) {
        // no-op
    }

    public void validateSAMLAssertion(Map context, Element assertion) {
        throw new UnsupportedOperationException();
    }

    public void validateSAMLAssertion(Map context, XMLStreamReader reader) {
        throw new UnsupportedOperationException();
    }

    public Element locateSAMLAssertion(Map context, Element binding, String assertionId, Document ownerDoc) {
        throw new UnsupportedOperationException();
    }

    public AuthenticationTokenPolicy.SAMLAssertionBinding populateSAMLPolicy(Map fpcontext,
                                                                             AuthenticationTokenPolicy.SAMLAssertionBinding policy,
                                                                             DynamicApplicationContext context) {
        throw new UnsupportedOperationException();
    }

    public boolean validateAndCacheNonce(Map map, String s, String s1, long l) throws XWSSecurityException {
        throw new UnsupportedOperationException();
    }

    public boolean isSelfCertificate(X509Certificate cert) {
        return false;
    }

    public KerberosContext doKerberosLogin() throws XWSSecurityException {
        throw new UnsupportedOperationException();
    }

    public KerberosContext doKerberosLogin(byte[] tokenValue) throws XWSSecurityException {
        throw new UnsupportedOperationException();
    }

    public void updateOtherPartySubject(Subject subject, GSSName clientCred, GSSCredential gssCred) {
        throw new UnsupportedOperationException();
    }

    private boolean expiresBeforeCreated(String creationTime, String expirationTime) throws XWSSecurityRuntimeException {
        Date created;
        Date expires = null;
        try {
            synchronized (calendarFormatter1) {
                created = calendarFormatter1.parse(creationTime);
                if (expirationTime != null) {
                    expires = calendarFormatter1.parse(expirationTime);
                }
            }
        } catch (java.text.ParseException pe) {
            synchronized (calendarFormatter2) {
                try {
                    created = calendarFormatter2.parse(creationTime);
                    if (expirationTime != null) {
                        expires = calendarFormatter2.parse(expirationTime);
                    }
                } catch (java.text.ParseException xpe) {
                    throw new XWSSecurityRuntimeException(xpe.getMessage());
                }
            }
        }

        return (expires != null) && expires.equals(created) || (expires != null) && expires.before(created);

    }

    private Date getGMTDateWithSkewAdjusted(Calendar c, long maxClockSkew, boolean addSkew) {
        long offset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            offset += c.getTimeZone().getDSTSavings();
        }
        long beforeTime = c.getTimeInMillis();
        long currentTime = beforeTime - offset;

        if (addSkew) {
            currentTime = currentTime + maxClockSkew;
        } else {
            currentTime = currentTime - maxClockSkew;
        }

        c.setTimeInMillis(currentTime);
        return c.getTime();
    }


    private Date getFreshnessAndSkewAdjustedDate(long maxClockSkew, long timestampFreshnessLimit) {
        Calendar c = new GregorianCalendar();
        long offset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            offset += c.getTimeZone().getDSTSavings();
        }
        long beforeTime = c.getTimeInMillis();
        long currentTime = beforeTime - offset;

        long adjustedTime = currentTime - maxClockSkew - timestampFreshnessLimit;
        c.setTimeInMillis(adjustedTime);

        return c.getTime();
    }


    private X509Certificate getDefaultCertificateInternal(KeyStore store, Map context) throws XWSSecurityRuntimeException {
        try {
            String alias = (String) context.get(MetroConstants.KEYSTORE_ALIAS);
            if (alias != null) {
                Certificate certificate = store.getCertificate(alias);
                if (certificate == null) {
                    throw new XWSSecurityRuntimeException("Certificate not found for alias in keystore: " + alias);
                } else if (!(certificate instanceof X509Certificate)) {
                    throw new XWSSecurityRuntimeException("Not an X.509 certificate: " + alias);
                }
                return (X509Certificate) certificate;
            }
            Object obj = context.get(XWSSConstants.CERTIFICATE_PROPERTY);
            if (obj instanceof X509Certificate) {
                return (X509Certificate) obj;
            }
            // if the keystore has only one key, use it
            Enumeration aliases = store.aliases();
            while (aliases.hasMoreElements()) {
                String currentAlias = (String) aliases.nextElement();
                if (store.isKeyEntry(currentAlias)) {
                    Certificate thisCertificate = store.getCertificate(currentAlias);
                    if (thisCertificate != null) {
                        if (thisCertificate instanceof X509Certificate) {
                            if (alias == null) {
                                alias = currentAlias;
                            } else {
                                // Not unique!
                                alias = null;
                                break;
                            }
                        }
                    }
                }
            }
            if (alias == null) {
                throw new XWSSecurityRuntimeException("Unable to determine alias for default certificate in keystore");
            }
            return (X509Certificate) store.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new XWSSecurityRuntimeException(e);
        }
    }

    private void checkEnabled() throws XWSSecurityRuntimeException {
        if (keyStore == null) {
            throw new XWSSecurityRuntimeException("Keystore not configured");
        }
        if (trustStore == null) {
            throw new XWSSecurityRuntimeException("Truststore not configured");
        }
    }
}