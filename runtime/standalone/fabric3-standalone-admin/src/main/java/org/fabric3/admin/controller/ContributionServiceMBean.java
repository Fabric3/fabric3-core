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
package org.fabric3.admin.controller;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.contribution.ArtifactValidationFailure;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionInUseException;
import org.fabric3.host.contribution.ContributionLockedException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.management.contribution.ArtifactErrorInfo;
import org.fabric3.management.contribution.ContributionInUseManagementException;
import org.fabric3.management.contribution.ContributionInfo;
import org.fabric3.management.contribution.ContributionInstallException;
import org.fabric3.management.contribution.ContributionLockedManagementException;
import org.fabric3.management.contribution.ContributionRemoveException;
import org.fabric3.management.contribution.ContributionUninstallException;
import org.fabric3.management.contribution.ErrorInfo;
import org.fabric3.management.contribution.InvalidContributionException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.host.ServletHost;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
@Management(name = "ContributionService", group = "deployment", description = "Manages contributions in a domain")
public class ContributionServiceMBean {
    private static final String REPOSITORY = "/admin/repository";
    private static final String PROFILE = "/admin/profile";

    private ServletHost servletHost;
    private ContributionService contributionService;
    private MetaDataStore metaDataStore;
    private ContributionServiceMBeanMonitor monitor;
    private String hostName;
    private String contributionAddress;
    private String profileAddress;

    public ContributionServiceMBean(@Reference ServletHost servletHost,
                                    @Reference ContributionService contributionService,
                                    @Reference MetaDataStore metaDataStore,
                                    @Monitor ContributionServiceMBeanMonitor monitor) {
        this.servletHost = servletHost;
        this.contributionService = contributionService;
        this.metaDataStore = metaDataStore;
        this.monitor = monitor;
    }

    /**
     * Optionally sets the host name to use for determining the IP address for clients to use when uploading contributions to a multi-homed machine.
     *
     * @param hostName the host name
     */
    @Property
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Init
    public void init() throws Exception {
        initAddress();
        // map a servlet for uploading contributions
        ContributionServlet contributionServlet = new ContributionServlet(contributionService, monitor);
        ProfileServlet profileServlet = new ProfileServlet(contributionService, monitor);
        servletHost.registerMapping(REPOSITORY + "/*", contributionServlet);
        servletHost.registerMapping(PROFILE + "/*", profileServlet);
    }

    @ManagementOperation(description = "Returns the network address the service receives contribution archives on")
    public String getContributionServiceAddress() {
        return contributionAddress;
    }

    @ManagementOperation(description = "Returns the network address the service receives profile archives on")
    public String getProfileServiceAddress() {
        return profileAddress;
    }

    @ManagementOperation(description = "Returns metadata for all contributions deployed in a domain")
    public Set<ContributionInfo> getContributions() {
        Set<Contribution> contributions = metaDataStore.getContributions();
        Set<ContributionInfo> infos = new TreeSet<ContributionInfo>();
        for (Contribution contribution : contributions) {
            URI uri = contribution.getUri();
            String state = contribution.getState().toString();
            long timestamp = contribution.getTimestamp();
            List<QName> deployables = new ArrayList<QName>();
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                deployables.add(deployable.getName());
            }
            ContributionInfo info = new ContributionInfo(uri, state, deployables, timestamp);
            infos.add(info);
        }
        return infos;
    }

    @ManagementOperation(description = "Installs a stored contribution")
    public void install(URI uri) throws ContributionInstallException {
        try {
            contributionService.install(uri);
        } catch (ValidationException e) {
            // propagate validation error messages to the client
            List<ErrorInfo> errors = new ArrayList<ErrorInfo>();
            for (ValidationFailure failure : e.getErrors()) {
                if (failure instanceof ArtifactValidationFailure) {
                    ArtifactValidationFailure avf = (ArtifactValidationFailure) failure;
                    ArtifactErrorInfo error = new ArtifactErrorInfo(avf.getArtifactName());
                    for (ValidationFailure entry : avf.getFailures()) {
                        ErrorInfo info = new ErrorInfo(entry.getMessage());
                        error.addError(info);
                    }
                    errors.add(error);
                } else {
                    ErrorInfo info = new ErrorInfo(failure.getMessage());
                    errors.add(info);
                }
            }
            throw new InvalidContributionException("Error installing " + uri, errors);
        } catch (ContributionException e) {
            monitor.error("Error installing: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            throw new ContributionInstallException(getErrorMessage(e));
        }
    }

    @ManagementOperation(description = "Uninstalls a contribution")
    public void uninstall(URI uri) throws ContributionUninstallException {
        try {
            contributionService.uninstall(uri);
        } catch (ContributionInUseException e) {
            throw new ContributionInUseManagementException(e.getMessage(), e.getUri(), e.getContributions());
        } catch (ContributionLockedException e) {
            throw new ContributionLockedManagementException(e.getMessage(), e.getUri(), e.getDeployables());
        } catch (ContributionException e) {
            // log the exception as it is not recoverable
            monitor.error("Error uninstalling contribution: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            throw new ContributionUninstallException(getErrorMessage(e));
        }
    }

    @ManagementOperation(description = "Removes a contribution")
    public void remove(URI uri) throws ContributionRemoveException {
        try {
            contributionService.remove(uri);
        } catch (ContributionException e) {
            monitor.error("Error removing contribution: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            throw new ContributionRemoveException(getErrorMessage(e));
        }
    }

    @ManagementOperation(description = "Installs a profile")
    public void installProfile(URI uri) throws ContributionInstallException {
        try {
            contributionService.installProfile(uri);
        } catch (ValidationException e) {
            // propagate validation error messages to the client
            List<ErrorInfo> errors = new ArrayList<ErrorInfo>();
            for (ValidationFailure failure : e.getErrors()) {
                if (failure instanceof ArtifactValidationFailure) {
                    ArtifactValidationFailure avf = (ArtifactValidationFailure) failure;
                    ArtifactErrorInfo error = new ArtifactErrorInfo(avf.getArtifactName());
                    for (ValidationFailure entry : avf.getFailures()) {
                        ErrorInfo info = new ErrorInfo(entry.getMessage());
                        error.addError(info);
                    }
                    errors.add(error);
                } else {
                    ErrorInfo info = new ErrorInfo(failure.getMessage());
                    errors.add(info);
                }
            }
            throw new InvalidContributionException("Error installing " + uri, errors);
        } catch (ContributionException e) {
            monitor.error("Error installing: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            throw new ContributionInstallException(getErrorMessage(e));
        }
    }

    @ManagementOperation(description = "Uninstalls a profile")
    public void uninstallProfile(URI uri) throws ContributionUninstallException {
        try {
            contributionService.uninstallProfile(uri);
        } catch (ContributionInUseException e) {
            throw new ContributionInUseManagementException(e.getMessage(), e.getUri(), e.getContributions());
        } catch (ContributionLockedException e) {
            throw new ContributionLockedManagementException(e.getMessage(), e.getUri(), e.getDeployables());
        } catch (ContributionException e) {
            // log the exception as it is not recoverable
            monitor.error("Error uninstalling profile: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            throw new ContributionUninstallException(getErrorMessage(e));
        }
    }

    @ManagementOperation(description = "Removes a profile from the domain")
    public void removeProfile(URI uri) throws ContributionRemoveException {
        try {
            contributionService.removeProfile(uri);
        } catch (ContributionException e) {
            monitor.error("Error removing profile: " + uri, e);
            // don't rethrow the original exception since the class will not be available on the client's classpath
            throw new ContributionRemoveException(getErrorMessage(e));
        }
    }

    /**
     * Calculates the addresses clients use to upload contributions.
     *
     * @throws UnknownHostException if the specified host is not found
     */
    private void initAddress() throws UnknownHostException {
        String baseAddress;
        if (hostName == null) {
            baseAddress = InetAddress.getLocalHost().getHostAddress();
        } else {
            baseAddress = InetAddress.getByName(hostName).getHostAddress();
        }
        contributionAddress = "http://" + baseAddress + ":" + servletHost.getHttpPort() + REPOSITORY;
        profileAddress = "http://" + baseAddress + ":" + servletHost.getHttpPort() + PROFILE;
    }

    private String getErrorMessage(ContributionException e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}
