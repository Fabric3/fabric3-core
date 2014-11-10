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
package org.fabric3.fabric.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.security.KeyStoreManager;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Configures the JVM to use the specified key and trust stores.
 */
@EagerInit
public class KeyStoreManagerImpl implements KeyStoreManager {
    private HostInfo info;
    private String keyStoreLocationProperty = "javax.net.ssl.keyStore";
    private String keyStoreLocation;
    private String keyStorePasswordProperty = "javax.net.ssl.keyStorePassword";
    private String keyStorePassword;
    private String trustStoreLocation;
    private String trustStoreLocationProperty = "javax.net.ssl.trustStore";
    private String trustStorePassword;
    private String trustStorePasswordProperty = "javax.net.ssl.trustStorePassword";
    private File keystoreFile;
    private File truststoreFile;
    private String certPassword;
    private String trustStoreType = "JKS";
    private String keyStoreType = "JKS";
    private KeyStore keyStore;
    private KeyStore trustStore;

    private String previousKeyStorePassword;
    private String previousKeyStoreLocationProperty;
    private String previousTrustStorePasswordProperty;
    private String previousTrustStoreLocationProperty;

    public KeyStoreManagerImpl(@Reference HostInfo info) {
        this.info = info;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:keystore.property")
    public void setKeyStoreLocationProperty(String keyStoreLocationProperty) {
        this.keyStoreLocationProperty = keyStoreLocationProperty;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:keystore.password.property")
    public void setKeyStorePasswordProperty(String keyStorePasswordProperty) {
        this.keyStorePasswordProperty = keyStorePasswordProperty;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:truststore.property")
    public void setTrustStoreLocationProperty(String trustStoreLocationProperty) {
        this.trustStoreLocationProperty = trustStoreLocationProperty;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:truststore.password.property")
    public void setTrustStorePasswordProperty(String trustStorePasswordProperty) {
        this.trustStorePasswordProperty = trustStorePasswordProperty;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:keystore")
    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:keystore.password")
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:truststore")
    public void setTrustStoreLocation(String trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:truststore.password")
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:cert.password")
    public void setCertPassword(String certPassword) {
        this.certPassword = certPassword;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:keystore.type")
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:security/f3:truststore.type")
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    @Init
    public void init() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        initializeKeyStore();
        initializeTrustStore();
    }

    @Destroy
    public void destroy() {
        if (previousKeyStorePassword != null) {
            System.setProperty(keyStorePasswordProperty, previousKeyStorePassword);
        }
        if (previousKeyStoreLocationProperty != null) {
            System.setProperty(keyStoreLocationProperty, previousKeyStoreLocationProperty);
        }
        if (previousTrustStorePasswordProperty != null) {
            System.setProperty(trustStorePasswordProperty, previousTrustStorePasswordProperty);
        }
        if (previousTrustStoreLocationProperty != null) {
            System.setProperty(trustStoreLocationProperty, previousTrustStoreLocationProperty);
        }
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public KeyStore getTrustStore() {
        return trustStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public File getKeyStoreLocation() {
        return keystoreFile;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public File getTrustStoreLocation() {
        return truststoreFile;
    }

    public String getCertPassword() {
        return certPassword;
    }

    private void initializeKeyStore() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        keystoreFile = null;
        if (keyStoreLocation == null) {
            String property = System.getProperty(keyStoreLocationProperty);
            if (property != null) {
                keystoreFile = new File(property);
                keyStoreLocation = keystoreFile.getAbsolutePath();
            } else {
                File dir = info.getBaseDir();
                if (dir != null) {
                    keystoreFile = new File(dir, "config" + File.separator + "fabric3-keystore.jks");
                    keyStoreLocation = keystoreFile.getAbsolutePath();
                }
            }
        } else {
            keystoreFile = new File(keyStoreLocation);
            if (!keystoreFile.isAbsolute()) {
                File dir = info.getBaseDir();
                keystoreFile = new File(dir, "config" + File.separator + keyStoreLocation);
                keyStoreLocation = keystoreFile.getAbsolutePath();
            }
        }
        if (keyStorePassword != null) {
            previousKeyStorePassword = System.setProperty(keyStorePasswordProperty, keyStorePassword);
        }
        if (keystoreFile != null && keystoreFile.exists()) {
            previousKeyStoreLocationProperty = System.setProperty(keyStoreLocationProperty, keystoreFile.getAbsolutePath());

            char[] keyStorePasswordChars = null;
            if (keyStorePassword != null) {
                keyStorePasswordChars = keyStorePassword.toCharArray();
            }
            keyStore = KeyStore.getInstance(keyStoreType);
            InputStream stream = new FileInputStream(keyStoreLocation);
            keyStore.load(stream, keyStorePasswordChars);

        }
    }

    private void initializeTrustStore() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        truststoreFile = null;
        if (trustStoreLocation == null) {
            File dir = info.getBaseDir();
            File file = new File(dir, "config" + File.separator + "fabric3-keystore.jks");
            if (dir != null && file.exists()) {
                truststoreFile = file;
                trustStoreLocation = truststoreFile.getAbsolutePath();
                trustStorePassword = keyStorePassword;
            } else if (keyStoreLocation != null) {
                // default to keystore values
                truststoreFile = keystoreFile;
                trustStoreLocation = keyStoreLocation;
                trustStorePassword = keyStorePassword;
            }
        } else {
            truststoreFile = new File(trustStoreLocation);
        }
        if (trustStorePassword != null) {
            previousTrustStorePasswordProperty = System.setProperty(trustStorePasswordProperty, trustStorePassword);
        }
        if (truststoreFile != null && truststoreFile.exists()) {
            previousTrustStoreLocationProperty = System.setProperty(trustStoreLocationProperty, truststoreFile.getAbsolutePath());
            trustStore = KeyStore.getInstance(trustStoreType);
            InputStream stream = new FileInputStream(trustStoreLocation);
            char[] trustStorePasswordChars = null;
            if (trustStorePassword != null) {
                trustStorePasswordChars = trustStorePassword.toCharArray();
            }
            trustStore.load(stream, trustStorePasswordChars);

        }
    }

}
