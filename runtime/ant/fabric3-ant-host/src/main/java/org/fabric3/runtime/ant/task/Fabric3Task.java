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
package org.fabric3.runtime.ant.task;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;

import org.fabric3.host.Names;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.BootstrapFactory;
import org.fabric3.host.runtime.BootstrapHelper;
import org.fabric3.host.runtime.BootstrapService;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.MaskingClassLoader;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ScanResult;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.util.FileHelper;
import org.fabric3.runtime.ant.api.TestRunner;
import org.fabric3.runtime.ant.monitor.AntMonitorEventDispatcher;

/**
 * Launches a Fabric3 instance from the Ant runtime distribution.
 * <p/>
 * To define the task, create a <code>taskdef</code> pointing to the Fabric3 ant runtime distribution /lib directory entry as follows:
 * <pre>
 * <p/>
 *  &lt;taskdef name="fabric3" classname="org.fabric3.runtime.ant.task.Fabric3Task"&gt;
 *       &lt;classpath&gt;
 *           &lt;fileset dir="&lt;path to distribution&gt;/fabric3-runtime-ant-1.6-bin/lib"&gt;
 *               &lt;include name="*.jar"/&gt;
 *           &lt;/fileset&gt;
 *       &lt;/classpath&gt;
 *   &lt;/taskdef&gt;
 * </pre>
 * This Task may be configured with <code>contribution</code> sub-elements which are <code>FileLists</code> pointing to contribution jars or
 * <code>contributionSet</code> sub-elements which are <code>FileSet</code> filters for calculating sets of contributions as follows:
 * <pre>
 * <p/>
 *  &lt;fabric3&gt;
 *     &lt;contribution dir="lib" files="mycontribution.jar"/&gt;
 *     &lt;contributionSet dir="build"&gt;
 *        &lt;include name="..."/&gt;
 *     &lt;/contributionSet&gt;
 *  &lt;/fabric3&gt;
 * </pre>
 *
 * @version $Rev$ $Date$
 */
public class Fabric3Task extends Task {
    private List<FileList> contributions = new ArrayList<FileList>();
    private List<FileSet> contributionSets = new ArrayList<FileSet>();

    private Fabric3Runtime runtime;
    private RuntimeCoordinator coordinator;

    public void addContribution(FileList contribution) {
        this.contributions.add(contribution);
    }

    public void addContributionSet(FileSet contribution) {
        this.contributionSets.add(contribution);
    }

    @Override
    public void execute() throws BuildException {
        log("Starting Fabric3 Runtime ...");
        startRuntime();
        deployContributions();
        executeTests();
        log("Stopping Fabric3 Runtime ...");
        stopRuntime();
    }

    private void startRuntime() throws BuildException {
        try {
            File installDirectory = BootstrapHelper.getInstallDirectory(Fabric3Task.class);

            //  calculate config directories based on the mode the runtime is booted in
            File configDir = BootstrapHelper.getDirectory(installDirectory, "config");
            File modeConfigDir = BootstrapHelper.getDirectory(configDir, RuntimeMode.VM.toString().toLowerCase());

            File bootDir = BootstrapHelper.getDirectory(installDirectory, "boot");
            File hostDir = BootstrapHelper.getDirectory(installDirectory, "host");

            // create the classloaders for booting the runtime
            ClassLoader systemClassLoader = getClass().getClassLoader();
            // mask hidden JDK and system classpath packages
            ClassLoader maskingClassLoader = new MaskingClassLoader(systemClassLoader, HiddenPackages.getPackages());
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(maskingClassLoader, hostDir);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

            BootstrapService bootstrapService = BootstrapFactory.getService(bootLoader);

            // load the system configuration
            Document systemConfig = bootstrapService.loadSystemConfig(modeConfigDir);

            URI domainName = bootstrapService.parseDomainName(systemConfig);

            // create the HostInfo and runtime
            HostInfo hostInfo = BootstrapHelper.createHostInfo(RuntimeMode.VM, domainName, installDirectory, configDir, modeConfigDir);
            // clear out the tmp directory
            FileHelper.cleanDirectory(hostInfo.getTempDir());

            MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer("fabric3");

            AntMonitorEventDispatcher dispatcher = new AntMonitorEventDispatcher(this);
            RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, mBeanServer, dispatcher);

            runtime = bootstrapService.createDefaultRuntime(runtimeConfig);

            Map<String, String> exportedPackages = new HashMap<String, String>();
            exportedPackages.put("org.fabric3.runtime.ant.api", Names.VERSION);

            URL systemComposite = new File(configDir, "system.composite").toURI().toURL();

            ScanResult result = bootstrapService.scanRepository(hostInfo.getRepositoryDirectory());

            BootConfiguration configuration = new BootConfiguration();
            configuration.setRuntime(runtime);
            configuration.setHostClassLoader(hostLoader);
            configuration.setBootClassLoader(bootLoader);
            configuration.setSystemCompositeUrl(systemComposite);
            configuration.setSystemConfig(systemConfig);
            configuration.setExtensionContributions(result.getExtensionContributions());
            configuration.setUserContributions(result.getUserContributions());
            configuration.setExportedPackages(exportedPackages);

            // boot the runtime
            coordinator = bootstrapService.createCoordinator(configuration);
            coordinator.start();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void stopRuntime() throws BuildException {
        try {
            coordinator.shutdown();
        } catch (ShutdownException e) {
            throw new BuildException(e);
        }
    }

    private void deployContributions() throws BuildException {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        for (FileList list : contributions) {
            Project project = list.getProject();
            File dir = list.getDir(project);
            for (String file : list.getFiles(project)) {
                FileContributionSource source = createContributionSource(dir, file);
                sources.add(source);
            }
        }
        for (FileSet set : contributionSets) {
            Project project = set.getProject();
            File dir = set.getDir(project);
            FileScanner scanner = set.getDirectoryScanner(project);
            set.setupDirectoryScanner(scanner, project);
            scanner.scan();

            String[] files = scanner.getIncludedFiles();
            for (String file : files) {
                FileContributionSource source = createContributionSource(dir, file);
                sources.add(source);
            }
        }

        try {
            ContributionService contributionService = runtime.getComponent(ContributionService.class, Names.CONTRIBUTION_SERVICE_URI);
            Domain domain = runtime.getComponent(Domain.class, Names.APPLICATION_DOMAIN_URI);
            List<URI> installed = contributionService.contribute(sources);
            for (URI contribution : installed) {
                log("Installed: " + contribution);
            }
            domain.include(installed);
        } catch (ContributionException e) {
            throw new BuildException(e);
        } catch (DeploymentException e) {
            throw new BuildException(e);
        }
    }

    private FileContributionSource createContributionSource(File dir, String file) throws BuildException {
        try {
            File contributionFile = new File(dir, file);
            URI uri = URI.create(contributionFile.getName());
            URL url = contributionFile.toURI().toURL();
            long timestamp = System.currentTimeMillis();
            return new FileContributionSource(uri, url, timestamp);
        } catch (MalformedURLException e) {
            throw new BuildException(e);
        }

    }

    private void executeTests() {
        TestRunner runner = runtime.getComponent(TestRunner.class, TestRunner.TEST_RUNNER_URI);
        runner.executeTests();
    }
}
