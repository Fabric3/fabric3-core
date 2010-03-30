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
package org.fabric3.admin.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import static javax.management.remote.JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER;
import static javax.management.remote.JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES;
import javax.management.remote.JMXServiceURL;
import static javax.naming.Context.SECURITY_CREDENTIALS;
import static javax.naming.Context.SECURITY_PRINCIPAL;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.management.contribution.ContributionInUseManagementException;
import org.fabric3.management.contribution.ContributionInfo;
import org.fabric3.management.contribution.ContributionInstallException;
import org.fabric3.management.contribution.ContributionLockedManagementException;
import org.fabric3.management.contribution.ContributionManagementException;
import org.fabric3.management.contribution.ContributionUninstallException;
import org.fabric3.management.contribution.DuplicateContributionManagementException;
import org.fabric3.management.contribution.InvalidContributionException;
import org.fabric3.management.domain.ComponentInfo;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.InvalidDeploymentException;

/**
 * Default implementation of the DomainController API.
 *
 * @version $Rev$ $Date$
 */
public class DomainControllerImpl implements DomainController {
    private static final String CONTRIBUTION_SERVICE_MBEAN = "fabric3:SubDomain=runtime, type=component, name=ContributionServiceMBean";
    private static final String DOMAIN_MBEAN = "fabric3:SubDomain=runtime, type=component, name=DistributedDomainMBean";

    private static final String RUNTIME_DOMAIN_MBEAN = "fabric3:SubDomain=runtime, type=component, name=RuntimeDomainMBean";

    private String username;
    private String password;
    private String domainAddress = "service:jmx:rmi:///jndi/rmi://localhost:1199/server";
    private String protocolPackages;

    private JMXConnector jmxc;

    public void setDomainAddress(String address) {
        domainAddress = address;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProtocolPackages(String protocolPackages) {
        this.protocolPackages = protocolPackages;
    }

    public void store(URL contribution, URI uri) throws CommunicationException, ContributionManagementException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            // find HTTP port and post contents
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();

            // store the contribution using a chunked HTTP post to the ContributionService
            String address;
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            address = (String) conn.getAttribute(oName, "ContributionServiceAddress");

            URL url = new URL(address + "/" + uri);
            int code = upload(contribution, url);
            if (200 == code) {
                return;
            } else if (420 == code) {
                throw new DuplicateContributionManagementException("A contribution already exists for " + uri);
            } else {
                throw new ContributionManagementException("Error storing contribution: " + code);
            }

        } catch (MBeanException e) {
            throw new ContributionManagementException(e.getMessage(), e.getTargetException());
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void install(URI uri) throws CommunicationException, ContributionInstallException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            // find HTTP port and post contents
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();

            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            // install the contribution
            conn.invoke(oName, "install", new URI[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof InvalidContributionException) {
                throw (InvalidContributionException) e.getTargetException();
            } else {
                throw new ContributionInstallException(e.getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public Set<ContributionInfo> stat() throws CommunicationException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            return (Set<ContributionInfo>) conn.getAttribute(oName, "Contributions");
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void deploy(URI uri) throws CommunicationException, DeploymentManagementException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            conn.invoke(oName, "deploy", new URI[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof InvalidDeploymentException) {
                throw (InvalidDeploymentException) e.getTargetException();
            } else {
                throw new DeploymentManagementException(e.getTargetException().getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }

    }

    public void deploy(URI uri, String plan) throws CommunicationException, DeploymentManagementException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            conn.invoke(oName, "deploy", new Object[]{uri, plan}, new String[]{URI.class.getName(), "java.lang.String"});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof InvalidDeploymentException) {
                throw (InvalidDeploymentException) e.getTargetException();
            } else {
                throw new DeploymentManagementException(e.getTargetException().getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void undeploy(URI uri) throws CommunicationException, DeploymentManagementException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            conn.invoke(oName, "undeploy", new Object[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            throw new DeploymentManagementException(e.getTargetException().getMessage(), e.getTargetException());
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void uninstall(URI name) throws CommunicationException, ContributionUninstallException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            conn.invoke(oName, "uninstall", new Object[]{name}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof ContributionInUseManagementException) {
                throw (ContributionInUseManagementException) e.getTargetException();
            } else if (e.getTargetException() instanceof ContributionLockedManagementException) {
                throw (ContributionLockedManagementException) e.getTargetException();
            } else {
                throw new ContributionUninstallException(e.getTargetException().getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void remove(URI name) throws CommunicationException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            conn.invoke(oName, "remove", new Object[]{name}, new String[]{URI.class.getName()});
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void storeProfile(URL profile, URI uri) throws CommunicationException, ContributionManagementException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            // find HTTP port and post contents
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();

            // store the contribution using a chunked HTTP post to the ContributionService
            String address;
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            address = (String) conn.getAttribute(oName, "ProfileServiceAddress");

            URL url = new URL(address + "/" + uri);
            int code = upload(profile, url);
            if (400 == code) {
                throw new ContributionManagementException("Error storing profile");
            } else if (420 == code) {
                throw new DuplicateContributionManagementException("A profile already exists for " + uri);
            }

        } catch (MBeanException e) {
            throw new ContributionManagementException(e.getMessage(), e.getTargetException());
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void installProfile(URI uri) throws CommunicationException, ContributionInstallException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            // find HTTP port and post contents
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();

            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            // install the contribution
            conn.invoke(oName, "installProfile", new URI[]{uri}, new String[]{URI.class.getName()});
            oName = new ObjectName(RUNTIME_DOMAIN_MBEAN);

            // deploy the contributions in the profile
            conn.invoke(oName, "deployProfile", new URI[]{uri}, new String[]{URI.class.getName()});

        } catch (MBeanException e) {
            if (e.getTargetException() instanceof InvalidContributionException) {
                throw (InvalidContributionException) e.getTargetException();
            } else {
                throw new ContributionInstallException(e.getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void uninstallProfile(URI uri) throws CommunicationException, ContributionUninstallException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(RUNTIME_DOMAIN_MBEAN);

            // deploy the contributions in the profile
            conn.invoke(oName, "undeployProfile", new URI[]{uri}, new String[]{URI.class.getName()});

            oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            conn.invoke(oName, "uninstallProfile", new Object[]{uri}, new String[]{URI.class.getName()});
        } catch (MBeanException e) {
            if (e.getTargetException() instanceof ContributionInUseManagementException) {
                throw (ContributionInUseManagementException) e.getTargetException();
            } else if (e.getTargetException() instanceof ContributionLockedManagementException) {
                throw (ContributionLockedManagementException) e.getTargetException();
            } else {
                throw new ContributionUninstallException(e.getTargetException().getMessage(), e.getTargetException());
            }
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public void removeProfile(URI name) throws CommunicationException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(CONTRIBUTION_SERVICE_MBEAN);
            conn.invoke(oName, "removeProfile", new Object[]{name}, new String[]{URI.class.getName()});
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<ComponentInfo> getDeployedComponents(String path) throws CommunicationException {
        try {
            if (!isConnected()) {
                throw new IllegalStateException("Not connected");
            }
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();
            ObjectName oName = new ObjectName(DOMAIN_MBEAN);
            return (List<ComponentInfo>) conn.invoke(oName, "getDeployedComponents", new Object[]{path}, new String[]{"java.lang.String"});
        } catch (JMException e) {
            throw new CommunicationException(e);
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    public boolean isConnected() {
        return jmxc != null;
    }

    public void connect() throws IOException {
        if (jmxc != null) {
            throw new IllegalStateException("Already connected");
        }
        JMXServiceURL url = new JMXServiceURL(domainAddress);
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        if (protocolPackages != null) {
            env.put(PROTOCOL_PROVIDER_PACKAGES, protocolPackages);
        }
        if (username != null) {
            env.put(SECURITY_PRINCIPAL, username);
        }
        if (password != null) {
            env.put(SECURITY_CREDENTIALS, password);
        }
        env.put(PROTOCOL_PROVIDER_CLASS_LOADER, getClass().getClassLoader());

        jmxc = JMXConnectorFactory.newJMXConnector(url, env);
        jmxc.connect();
    }

    public void disconnect() throws IOException {
        if (jmxc == null) {
            throw new IllegalStateException("Not connected");
        }
        try {
            jmxc.close();
        } finally {
            jmxc = null;
        }

    }

    private int upload(URL contribution, URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setChunkedStreamingMode(4096);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-type", "binary/octet-stream");

        InputStream is = null;
        OutputStream os = null;
        try {
            os = connection.getOutputStream();
            is = contribution.openStream();
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

        return connection.getResponseCode();
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
