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
        if (keyStorePassword != null) {
            System.clearProperty(keyStorePasswordProperty);
        }
        if (keystoreFile != null && keystoreFile.exists()) {
            System.clearProperty(keyStoreLocationProperty);
        }
        if (trustStorePassword != null) {
            System.clearProperty(trustStorePasswordProperty);
        }
        if (truststoreFile != null && truststoreFile.exists()) {
            System.clearProperty(trustStoreLocationProperty);
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
            System.setProperty(keyStorePasswordProperty, keyStorePassword);
        }
        if (keystoreFile != null && keystoreFile.exists()) {
            System.setProperty(keyStoreLocationProperty, keystoreFile.getAbsolutePath());

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
            System.setProperty(trustStorePasswordProperty, trustStorePassword);
        }
        if (truststoreFile != null && truststoreFile.exists()) {
            System.setProperty(trustStoreLocationProperty, truststoreFile.getAbsolutePath());
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
