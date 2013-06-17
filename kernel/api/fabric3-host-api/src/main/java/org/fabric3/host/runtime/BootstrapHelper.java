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
package org.fabric3.host.runtime;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.Version;
import org.fabric3.host.classloader.DelegatingResourceClassLoader;
import org.fabric3.host.os.OperatingSystem;
import org.fabric3.host.util.FileHelper;
import org.fabric3.host.util.OSHelper;

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
     * @throws FileNotFoundException if the boot directory does not exist
     */
    public static File getDirectory(File baseDir, String path) throws FileNotFoundException {
        File dir = new File(baseDir, path);
        if (!dir.exists()) {
            throw new FileNotFoundException("Directory does not exist: " + dir);
        }
        if (!dir.isDirectory()) {
            throw new FileNotFoundException("Resource is not a directory: " + dir);
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
        File[] jars = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
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
            }
        });

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
    public static void cloneRuntimeImage(File sourceConfigDir, File targetDir) throws IOException {
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
     * @param runtimeMode       the runtime boot mode
     * @param domainName        the name of the domain the runtime is part of
     * @param environment       the runtime environment
     * @param runtimeDir        the base directory containing non-sharable, read-write runtime artifacts
     * @param configDir         the root configuration directory
     * @param extensionsDir     the sharable extensions directory
     * @param deployDirectories additional deploy directories. These may be absolute or relative to the runtime directory.
     * @param javaEEXAEnabled   true if the host is a Java EE XA-enabled container
     * @return the host information
     * @throws IOException if there is an error accessing a host info directory
     */
    public static HostInfo createHostInfo(String runtimeName,
                                          RuntimeMode runtimeMode,
                                          URI domainName,
                                          String environment,
                                          File runtimeDir,
                                          File configDir,
                                          File extensionsDir,
                                          List<File> deployDirectories,
                                          boolean javaEEXAEnabled) throws IOException {
        File repositoryDir = getDirectory(runtimeDir, "repository");
        File userRepositoryDir = new File(repositoryDir, "user");
        File runtimeRepositoryDir = new File(repositoryDir, "runtime");
        File tempDir = getDirectory(runtimeDir, "tmp");
        File dataDir = getDirectory(runtimeDir, "data");
        File deployDir = new File(runtimeDir, "deploy");
        List<File> deployDirs = new ArrayList<File>();
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

        return new DefaultHostInfo(runtimeName,
                                   runtimeMode,
                                   environment,
                                   domainName,
                                   runtimeDir,
                                   userRepositoryDir,
                                   extensionsDir,
                                   runtimeRepositoryDir,
                                   configDir,
                                   dataDir,
                                   tempDir,
                                   deployDirs,
                                   os,
                                   javaEEXAEnabled);
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
