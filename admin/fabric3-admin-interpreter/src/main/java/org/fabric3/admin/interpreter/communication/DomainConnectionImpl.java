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
package org.fabric3.admin.interpreter.communication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.jaxrs.Annotations;
import org.codehaus.jackson.jaxrs.MapperConfigurator;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 */
public class DomainConnectionImpl implements DomainConnection {
    private static final String ADDRESS = "http://localhost:8180/management";
    private static final int TIMEOUT = 20000;
    private static final Annotations[] DEFAULT_ANNOTATIONS = {Annotations.JACKSON, Annotations.JAXB};
    private static final String KEY_STORE = "javax.net.ssl.keyStore";
    private static final String TRUST_STORE = "javax.net.ssl.trustStore";

    private LinkedList<String> aliases;
    private LinkedList<String> addresses;
    private String username;
    private String password;

    private ObjectMapper mapper;
    private SSLSocketFactory sslFactory;

    public DomainConnectionImpl() {
        MapperConfigurator configurator = new MapperConfigurator(null, DEFAULT_ANNOTATIONS);
        mapper = configurator.getDefaultMapper();
        aliases = new LinkedList<String>();
        addresses = new LinkedList<String>();
        aliases.add("default");
        addresses.add(ADDRESS);
    }

    public void setAddress(String alias, String address) {
        aliases.clear();
        addresses.clear();
        aliases.add(alias);
        addresses.add(address);
    }

    public void pushAddress(String alias, String address) {
        aliases.add(alias);
        addresses.add(address);
    }

    public String popAddress() {
        if (addresses.size() == 1) {
            return null;
        }
        aliases.removeLast();
        addresses.removeLast();
        return aliases.getLast();
    }

    public String getAlias() {
        return aliases.getLast();
    }

    public String getAddress() {
        return addresses.getLast();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @SuppressWarnings({"unchecked"})
    public <T> T parse(Class<?> type, InputStream stream) throws IOException {
        JsonParser jp = mapper.getJsonFactory().createJsonParser(stream);
        return (T) mapper.readValue(jp, type);
    }

    public void serialize(String message, OutputStream stream) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(message);
        stream.write(bytes);
    }

    public HttpURLConnection createControllerConnection(String path, String verb) throws CommunicationException {
        return createAddressConnection(addresses.getFirst(), path, verb);
    }

    public HttpURLConnection createConnection(String path, String verb) throws CommunicationException {
        return createAddressConnection(addresses.getLast(), path, verb);
    }

    public HttpURLConnection put(String path, URL resource) throws CommunicationException {
        try {
            URL url = createUrl(addresses.getLast() + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode(4096);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-type", "application/json");
            setBasicAuth(connection);

            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                setSocketFactory(httpsConnection);
            }

            InputStream is = null;
            OutputStream os = null;
            try {
                os = connection.getOutputStream();
                is = resource.openStream();
                copy(is, os);
                os.flush();
            } finally {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            }

            return connection;
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    private HttpURLConnection createAddressConnection(String address, String path, String verb) throws CommunicationException {

        try {
            URL url = createUrl(address + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod(verb);
            connection.setRequestProperty("Content-type", "application/json");
            connection.setReadTimeout(TIMEOUT);

            setBasicAuth(connection);

            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                setSocketFactory(httpsConnection);
            }

            return connection;
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    private int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private void setBasicAuth(HttpURLConnection connection) {
        if (username != null) {
            String header = username + ":" + password;
            String encoded = Base64.encode(header.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encoded);
        }
    }

    private void setSocketFactory(HttpsURLConnection connection) throws CommunicationException {
        try {
            if (sslFactory == null) {
                // initialize the SSL context
                String keyStoreLocation = getKeystoreLocation();
                if (keyStoreLocation == null) {
                    throw new CommunicationException("Keystore not configured. A keystore must be placed in /config when using SSL.");
                }
                System.setProperty(KEY_STORE, keyStoreLocation);
                System.setProperty(TRUST_STORE, keyStoreLocation);
                KeyStore keyStore = KeyStore.getInstance("JKS");
                InputStream stream = new FileInputStream(keyStoreLocation);
                keyStore.load(stream, null);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, tmf.getTrustManagers(), null);
                sslFactory = ctx.getSocketFactory();
            }
            connection.setSSLSocketFactory(sslFactory);
        } catch (NoSuchAlgorithmException e) {
            throw new CommunicationException(e);
        } catch (KeyStoreException e) {
            throw new CommunicationException(e);
        } catch (KeyManagementException e) {
            throw new CommunicationException(e);
        } catch (FileNotFoundException e) {
            throw new CommunicationException(e);
        } catch (CertificateException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    private String getKeystoreLocation() {
        File configDir = new File(getInstallDirectory(), "config");
        if (!configDir.exists() || !configDir.isDirectory()) {
            return null;
        }
        for (String file : configDir.list()) {
            if (file.endsWith(".jks") || file.endsWith(".keystore ")) {
                return new File(configDir, file).getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Gets the installation directory based on the location of a class file. The installation directory is calculated by determining the path of the
     * jar containing the given class file and returning its parent directory.
     *
     * @return directory where Fabric3 runtime is installed.
     * @throws IllegalStateException if the location could not be determined from the location of the class file
     */
    private File getInstallDirectory() throws IllegalStateException {
        // get the name of the Class's bytecode
        String name = getClass().getName();
        int last = name.lastIndexOf('.');
        if (last != -1) {
            name = name.substring(last + 1);
        }
        name = name + ".class";

        // get location of the bytecode - should be a jar: URL
        URL url = getClass().getResource(name);
        if (url == null) {
            throw new IllegalStateException("Unable to get location of bytecode resource " + name);
        }

        String jarLocation = url.toString();
        if (!jarLocation.startsWith("jar:")) {
            throw new IllegalStateException("Must be run from a jar: " + url);
        }

        // extract the location of thr jar from the resource URL
        jarLocation = jarLocation.substring(4, jarLocation.lastIndexOf("!/"));
        if (!jarLocation.startsWith("file:")) {
            throw new IllegalStateException("Must be run from a local filesystem: " + jarLocation);
        }

        File jarFile = new File(URI.create(jarLocation));
        return jarFile.getParentFile().getParentFile();
    }

    private URL createUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }


}
