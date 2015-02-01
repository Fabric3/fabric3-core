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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.host.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Version;
import org.fabric3.api.host.classloader.DelegatingResourceClassLoader;
import org.fabric3.api.host.os.OperatingSystem;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.api.host.util.OSHelper;
import org.fabric3.api.model.type.RuntimeMode;

/**
 * Utility class for bootstrap operations.
 */
public final class BootstrapHelper {

    private BootstrapHelper() {
    }

    /**
     * Gets the installation directory based on the location of a class file. The installation directory is calculated by determining the path of the jar
     * containing the given class file and returning its parent directory.
     *
     * @param clazz the class to use as a way to find the executable jar
     * @return directory where Fabric3 runtime is installed.
     * @throws IllegalStateException if the location could not be determined from the location of the class file
     */
    public static File getInstallDirectory(Class<?> clazz) throws IllegalStateException {
        // get the name of the Class's bytecode
        String name = clazz.getName();
        int last = name.lastIndexOf('.');
        if (last != -1) {
            name = name.substring(last + 1);
        }
        name = name + ".class";

        // get location of the bytecode - should be a jar: URL
        URL url = clazz.getResource(name);
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

    /**
     * Gets the directory for the specified base directory/path combination.
     *
     * @param baseDir the base directory
     * @param path    the  path
     * @return the boot directory
     * @throws Fabric3Exception if the boot directory does not exist
     */
    public static File getDirectory(File baseDir, String path) throws Fabric3Exception {
        File dir = new File(baseDir, path);
        if (!dir.exists()) {
            throw new Fabric3Exception("Directory does not exist: " + dir);
        }
        if (!dir.isDirectory()) {
            throw new Fabric3Exception("Resource is not a directory: " + dir);
        }
        return dir;
    }

    /**
     * Create a classloader from all the jar files or sub-directories in a directory. The classpath for the returned classloader will comprise all jar files and
     * sub-directories of the supplied directory. Hidden files and those that do not contain a valid manifest will be silently ignored.
     *
     * @param parent    the parent for the new classloader
     * @param directory the directory to scan
     * @return a classloader whose classpath includes all jar files and sub-directories of the supplied directory
     */
    public static ClassLoader createClassLoader(ClassLoader parent, File directory) {
        File[] jars = directory.listFiles(file -> {
            if (file.isHidden()) {
                return false;
            }
            if (file.isDirectory()) {
                return true;
            }
            try {
                JarFile jar = new JarFile(file);
                return jar.getManifest() != null;
            } catch (IOException e) {
                return false;
            }
        });
        if (jars == null) {
            return new DelegatingResourceClassLoader(new URL[0], parent);
        }
        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                // toURI should have escaped the URL
                throw new AssertionError();
            }
        }
        return new DelegatingResourceClassLoader(urls, parent);
    }

    /**
     * Creates a new read-write runtime directory
     *
     * @param sourceConfigDir the configuration directory to use as a template
     * @param targetDir       the target runtime directory
     * @throws IOException if the runtime directory cannot be created
     */
    public static void cloneRuntimeImage(File sourceConfigDir, File targetDir) {
        File targetConfigDir = new File(targetDir, "config");
        FileHelper.forceMkdir(targetConfigDir);
        FileHelper.copyDirectory(sourceConfigDir, targetConfigDir);
        FileHelper.forceMkdir(new File(targetDir, "data"));
        FileHelper.forceMkdir(new File(targetDir, "deploy"));
        File repository = new File(targetDir, "repository");
        FileHelper.forceMkdir(repository);
        FileHelper.forceMkdir(new File(repository, "runtime"));
        FileHelper.forceMkdir(new File(repository, "user"));
        FileHelper.forceMkdir(new File(targetDir, "tmp"));
    }

    /**
     * Creates the HostInfo for a runtime.
     *
     * @param runtimeName       the runtime name
     * @param zoneName          the zone name
     * @param runtimeMode       the runtime boot mode
     * @param domainName        the name of the domain the runtime is part of
     * @param environment       the runtime environment
     * @param runtimeDir        the base directory containing non-sharable, read-write runtime artifacts
     * @param extensionsDir     the sharable extensions directory
     * @param deployDirectories additional deploy directories. These may be absolute or relative to the runtime directory.
     * @param javaEEXAEnabled   true if the host is a Java EE XA-enabled container
     * @return the host information
     * @throws IOException if there is an error accessing a host info directory
     */
    public static HostInfo createHostInfo(String runtimeName,
                                          String zoneName,
                                          RuntimeMode runtimeMode,
                                          URI domainName,
                                          String environment,
                                          File runtimeDir,
                                          File extensionsDir,
                                          List<File> deployDirectories,
                                          boolean javaEEXAEnabled) throws IOException {
        File tempDir = getDirectory(runtimeDir, "tmp");
        File dataDir = getDirectory(runtimeDir, "data");
        File deployDir = new File(runtimeDir, "deploy");
        List<File> deployDirs = new ArrayList<>();
        for (File directory : deployDirectories) {
            if (!directory.isAbsolute()) {
                directory = new File(runtimeDir, directory.getName());
            }
            if (!deployDir.equals(directory) && !deployDirs.contains(directory)) {
                //avoid duplicates
                deployDirs.add(directory);
            }
        }
        deployDirs.add(deployDir);

        OperatingSystem os = getOperatingSystem();

        DefaultHostInfoBuilder builder = new DefaultHostInfoBuilder().runtimeName(runtimeName);
        builder.zoneName(zoneName);
        builder.runtimeMode(runtimeMode);
        builder.environment(environment);
        builder.domain(domainName);
        builder.baseDir(runtimeDir);
        builder.sharedDirectory(extensionsDir);
        builder.dataDirectory(dataDir);
        builder.tempDirectory(tempDir);
        builder.deployDirectories(deployDirs);
        builder.operatingSystem(os);
        builder.javaEEXAEnabled(javaEEXAEnabled);
        return builder.build();
    }

    /**
     * Returns the current {@link OperatingSystem}.
     *
     * @return the current OS
     */
    public static OperatingSystem getOperatingSystem() {
        String name = System.getProperty("os.name");
        String processor = OSHelper.parseProcessor(System.getProperty("os.arch"));
        String versionStr = OSHelper.parseVersion(System.getProperty("os.version"));
        Version version = Version.parseVersion(versionStr);
        return new OperatingSystem(name, processor, version);
    }

}
