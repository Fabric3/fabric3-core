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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.maven.itest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.RuntimeInformation;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import org.fabric3.api.host.Names;
import org.fabric3.api.host.contribution.ContributionNotFoundException;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.FileContributionSource;
import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.classloader.DelegatingResourceClassLoader;
import org.fabric3.api.host.runtime.HiddenPackages;
import org.fabric3.api.host.classloader.MaskingClassLoader;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.runtime.maven.MavenRuntime;

/**
 * Runs an embedded Fabric3 runtime for integration testing.
 * @goal test
 * @phase integration-test
 * @execute phase="integration-test"
 */
public class Fabric3ITestMojo extends AbstractMojo {
    private static final String CLEAN = "fabric3.extensions.dependencies.cleanup";

    static {
        // This static block is used to optionally clean the temporary directory between test runs. A static block is used as the iTest plugin may
        // be instantiated multiple times during a run.
        boolean clearTmp = Boolean.valueOf(System.getProperty(CLEAN, "false"));
        if (clearTmp) {
            clearTempFiles();
        }
    }

    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    /**
     * The optional target namespace of the composite to activate.
     *
     * @parameter
     */
    public String compositeNamespace = "urn:fabric3.org";

    /**
     * The local name of the composite to activate.
     *
     * @parameter
     */
    public String compositeName = "TestComposite";

    /**
     * The project build directory.
     *
     * @parameter expression="${project.build.directory}"
     */
    public File buildDirectory;

    /**
     * Do not run if this is set to true. This usage is consistent with the surefire plugin.
     *
     * @parameter expression="${maven.test.skip}"
     */
    public boolean skip;

    /**
     * The directory where reports will be written.
     *
     * @parameter expression="${project.build.directory}/surefire-reports"
     */
    public File reportsDirectory;

    /**
     * Whether to trim the stack trace in the reports to just the lines within the test, or show the full trace.
     *
     * @parameter expression="${trimStackTrace}" default-value="true"
     */
    public boolean trimStackTrace;

    /**
     * The version of the runtime to use.
     *
     * @parameter expression="RELEASE"
     */
    public String runtimeVersion;

    /**
     * Set of contributions that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] contributions = new Dependency[0];

    /**
     * Set of runtime extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] extensions = new Dependency[0];

    /**
     * Set of profiles for the runtime.
     *
     * @parameter
     */
    public Dependency[] profiles = new Dependency[0];

    /**
     * Libraries available to application and runtime.
     *
     * @parameter
     */
    public Dependency[] shared;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    public ArtifactRepository localRepository;

    /**
     * @parameter expression="${component.org.fabric3.runtime.maven.itest.ArtifactHelper}"
     * @required
     * @readonly
     */
    public ArtifactHelper artifactHelper;

    /**
     * @parameter expression="${component.org.fabric3.runtime.maven.itest.ExtensionHelper}"
     * @required
     * @readonly
     */
    public ExtensionHelper extensionHelper;

    /**
     * The sub-directory of the project's output directory which contains the systemConfig.xml file. Users are limited to specifying the (relative)
     * directory name in this param - the file name is fixed. The fixed name is not required by the itest environment but using it retains the
     * relationship between the test config file and WEB-INF/systemConfig.xml which contains the same information for the deployed composite
     *
     * @parameter
     */
    public String systemConfigDir;

    /**
     * Allows the optional in-line specification of system configuration in the plugin configuration.
     *
     * @parameter
     */
    public String systemConfig;

    /**
     * Build output directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * Allows the optional in-line specification of an expected error
     *
     * @parameter
     */
    public String errorText;

    /**
     * If false, external default repositories for transitive dependencies will not be used
     *
     * @parameter
     */
    public boolean useDefaultRepositories = true;

    /**
     * JDK and system classpath packages to hide from the runtime classpath.
     *
     * @parameter
     */
    public String[] hiddenPackages = HiddenPackages.getPackages();

    /**
     * @component
     */
    public RuntimeInformation runtimeInformation;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip || Boolean.parseBoolean(System.getProperty("maven.test.skip"))) {
            getLog().info("Skipping integration tests by user request.");
            return;
        }

        artifactHelper.setLocalRepository(localRepository);
        artifactHelper.setProject(project);

        MavenBootConfiguration configuration = createBootConfiguration();

        Thread.currentThread().setContextClassLoader(configuration.getBootClassLoader());

        MavenRuntimeBooter booter = new MavenRuntimeBooter(configuration);

        MavenRuntime runtime = booter.boot();
        try {
            // load the contributions
            deployContributions(runtime);
            TestDeployer deployer = new TestDeployer(compositeNamespace, compositeName, buildDirectory, getLog());
            boolean continueDeployment = deployer.deploy(runtime, errorText);
            if (!continueDeployment) {
                return;
            }
            TestRunner runner = new TestRunner(reportsDirectory, trimStackTrace, getLog());
            runner.executeTests(runtime);
        } catch (RuntimeException e) {
            // log unexpected errors since Maven sometimes swallows them
            getLog().error(e);
            throw e;
        } finally {
            try {
                tryLatch(runtime);
                booter.shutdown();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Waits on a latch component if one is configured for the test run.
     *
     * @param runtime the runtime
     */
    private void tryLatch(MavenRuntime runtime) {
        Object latchComponent = runtime.getComponent(Object.class, TestConstants.TEST_LATCH_SERVICE);
        if (latchComponent != null) {
            Class<?> type = latchComponent.getClass();
            try {
                Method method = type.getDeclaredMethod("await");
                getLog().info("Waiting on Fabric3 runtime latch");
                method.invoke(latchComponent);
                getLog().info("Fabric3 runtime latch released");
            } catch (NoSuchMethodException e) {
                getLog().error("Found latch service " + type + " but it does not declare an await() method");
            } catch (SecurityException e) {
                getLog().error("Security exception introspecting latch service", e);
            } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                getLog().error("Exception attempting to wait on latch service", e);
            }
        }
    }

    /**
     * Resolves and deploys configured contributions.
     *
     * @param runtime the runtime
     * @throws MojoExecutionException if a deployment error occurs
     */
    private void deployContributions(MavenRuntime runtime) throws MojoExecutionException {
        if (contributions.length <= 0) {
            return;
        }
        try {
            ContributionService contributionService = runtime.getComponent(ContributionService.class, Names.CONTRIBUTION_SERVICE_URI);
            Domain domain = runtime.getComponent(Domain.class, Names.APPLICATION_DOMAIN_URI);
            List<ContributionSource> sources = new ArrayList<>();
            for (Dependency contribution : contributions) {
                Artifact artifact = artifactHelper.resolve(contribution, Collections.<ArtifactRepository>emptySet());
                URL url = artifact.getFile().toURI().toURL();
                URI uri = URI.create(new File(url.getFile()).getName());
                ContributionSource source = new FileContributionSource(uri, url, -1, true);
                sources.add(source);
            }
            List<URI> uris = contributionService.store(sources);
            contributionService.install(uris);
            domain.include(uris);
        } catch (MalformedURLException | InstallException | ContributionNotFoundException | DeploymentException | StoreException e) {
            throw new MojoExecutionException("Error installing contributions", e);
        }
    }

    /**
     * Recursively cleans the F3 temporary directory.
     */
    private static void clearTempFiles() {
        File f3TempDir = new File(System.getProperty("java.io.tmpdir"), ".f3");
        try {
            FileHelper.deleteDirectory(f3TempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the configuration to boot the Maven runtime, including resolving dependencies.
     *
     * @return the boot configuration
     * @throws MojoExecutionException if there is an error creating the configuration
     */
    private MavenBootConfiguration createBootConfiguration() throws MojoExecutionException {
        int mavenVersion = runtimeInformation.getApplicationVersion().getMajorVersion();
        Set<Artifact> runtimeArtifacts = artifactHelper.calculateRuntimeArtifacts(runtimeVersion, mavenVersion);
        Set<Artifact> hostArtifacts = artifactHelper.calculateHostArtifacts(runtimeArtifacts, shared);
        Set<Artifact> dependencies = artifactHelper.calculateDependencies();
        Set<URL> moduleDependencies = artifactHelper.calculateModuleDependencies(dependencies, hostArtifacts);

        Set<Dependency> expandedExtensions = new HashSet<>();
        expandedExtensions.addAll(getCoreExtensions());
        expandedExtensions.addAll(Arrays.asList(extensions));
        ExpandedProfiles expandedProfiles = artifactHelper.expandProfileExtensions(profiles);
        expandedExtensions.addAll(expandedProfiles.getExtensions());

        ClassLoader parentClassLoader = getClass().getClassLoader();
        if (hiddenPackages.length > 0) {
            // mask hidden JDK and system classpath packages
            parentClassLoader = new MaskingClassLoader(parentClassLoader, hiddenPackages);
        }


        ClassLoader hostClassLoader = createHostClassLoader(parentClassLoader, hostArtifacts);
        ClassLoader bootClassLoader = createBootClassLoader(hostClassLoader, runtimeArtifacts);

        MavenBootConfiguration configuration = new MavenBootConfiguration();
        configuration.setMavenVersion(mavenVersion);
        configuration.setBootClassLoader(bootClassLoader);
        configuration.setHostClassLoader(hostClassLoader);
        configuration.setLog(getLog());
        configuration.setExtensionHelper(extensionHelper);

        configuration.setExtensions(expandedExtensions);
        if (useDefaultRepositories) {
            configuration.setRemoteRepositories(expandedProfiles.getRepositories());
        }

        configuration.setModuleDependencies(moduleDependencies);
        configuration.setOutputDirectory(outputDirectory);
        configuration.setSystemConfig(systemConfig);
        configuration.setSystemConfigDir(systemConfigDir);
        return configuration;
    }

    /**
     * Creates the classloader to boot the runtime.
     *
     * @param parent    the parent classloader
     * @param artifacts the set of artifacts to include on the boot classpath
     * @return the boot classloader
     */
    private ClassLoader createBootClassLoader(ClassLoader parent, Set<Artifact> artifacts) {

        URL[] urls = new URL[artifacts.size()];
        int i = 0;
        for (Artifact artifact : artifacts) {
            File file = artifact.getFile();
            assert file != null;
            try {
                urls[i++] = file.toURI().toURL();
            } catch (MalformedURLException e) {
                // toURI should have made this valid
                throw new AssertionError(e);
            }
        }

        Log log = getLog();
        if (log.isDebugEnabled()) {
            log.debug("Fabric3 boot classpath:");
            for (URL url : urls) {
                log.debug("  " + url);
            }
        }
        return new DelegatingResourceClassLoader(urls, parent);
    }

    /**
     * Creates the host classloader based on the given set of artifacts.
     *
     * @param parent        the parent classloader
     * @param hostArtifacts the  artifacts
     * @return the host classloader
     */
    private ClassLoader createHostClassLoader(ClassLoader parent, Set<Artifact> hostArtifacts) {
        List<URL> urls = new ArrayList<>(hostArtifacts.size());
        for (Artifact artifact : hostArtifacts) {
            try {
                File pathElement = artifact.getFile();
                URL url = pathElement.toURI().toURL();
                getLog().debug("Adding artifact URL: " + url);
                urls.add(url);
            } catch (MalformedURLException e) {
                // toURI should have encoded the URL
                throw new AssertionError(e);
            }

        }
        return new DelegatingResourceClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }

    /**
     * Returns the core runtime extensions as a set of dependencies
     *
     * @return the extensions
     */
    private Set<Dependency> getCoreExtensions() {
        Set<Dependency> extensions = new HashSet<>();

        Dependency dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-jdk-proxy");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-channel-impl");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-java");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-async");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-sca-intents");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-resource");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-execution");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-maven-extension");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-junit");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("junit");
        dependency.setArtifactId("junit");
        dependency.setVersion(TestConstants.JUNIT_VERSION);
        dependency.setType("jar");
        extensions.add(dependency);

        return extensions;
    }

}
