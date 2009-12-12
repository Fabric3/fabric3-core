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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.jar.JarFile;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.monitor.MonitorFactory;

/**
 * Utility class for boostrap related operations.
 *
 * @version $Revision$ $Date$
 */
public final class BootstrapHelper {

    private static final String DEFAULT_MONITOR_FACTORY = "org.fabric3.monitor.impl.JavaLoggingMonitorFactory";
    private static final String BOOTSTRAPPER_CLASS = "org.fabric3.fabric.runtime.bootstrap.ScdlBootstrapperImpl";
    private static final String COORDINATOR_CLASS = "org.fabric3.fabric.runtime.DefaultCoordinator";
    private static final String RUNTIME_CLASS = "org.fabric3.fabric.runtime.DefaultRuntime";

    private BootstrapHelper() {
    }

    /**
     * Gets the installation directory based on the location of a class file. The installation directory is calculated by determining the path of the
     * jar containing the given class file and returning its parent directory.
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
     * Create a classloader from all the jar files or subdirectories in a directory. The classpath for the returned classloader will comprise all jar
     * files and subdirectories of the supplied directory. Hidden files and those that do not contain a valid manifest will be silently ignored.
     *
     * @param parent    the parent for the new classloader
     * @param directory the directory to scan
     * @return a classloader whose classpath includes all jar files and subdirectories of the supplied directory
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

        return new URLClassLoader(urls, parent);
    }

    /**
     * Load properties from the specified file. If the file does not exist then an empty properties object is returned.
     *
     * @param propFile the file to load from
     * @param defaults defaults for the properties
     * @return a Properties object loaded from the file
     * @throws IOException if there was a problem loading the properties
     */
    public static Properties loadProperties(File propFile, Properties defaults) throws IOException {
        Properties props = defaults == null ? new Properties() : new Properties(defaults);
        FileInputStream is;
        try {
            is = new FileInputStream(propFile);
        } catch (FileNotFoundException e) {
            return props;
        }
        try {
            props.load(is);
            return props;
        } finally {
            is.close();
        }
    }

    public static HostInfo createHostInfo(RuntimeMode runtimeMode, File baseDir, File configDir, File modeDir, Properties props)
            throws InitializationException, IOException {

        File repositoryDir = getDirectory(baseDir, "repository");
        File tempDir = getDirectory(baseDir, "tmp");
        File dataDir = getDirectory(baseDir, "data");

        try {

            // set the domain from runtime properties
            String domainName = props.getProperty("domain");
            URI domain;
            if (domainName != null) {
                domain = new URI(domainName);
            } else {
                throw new InitializationException("Domain URI was not set. Ensure it is set as a system property or in runtime.properties.");
            }

            return new DefaultHostInfo(runtimeMode, domain, baseDir, repositoryDir, configDir, modeDir, props, tempDir, dataDir);
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getMessage());
        }

    }

    public static MonitorFactory createDefaultMonitorFactory(ClassLoader classLoader) throws InitializationException {
        return createMonitorFactory(classLoader, DEFAULT_MONITOR_FACTORY);
    }

    public static MonitorFactory createMonitorFactory(ClassLoader classLoader, String factoryClass) throws InitializationException {
        try {
            Class<?> monitorClass = Class.forName(factoryClass, true, classLoader);
            return (MonitorFactory) monitorClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new InitializationException(e);
        } catch (IllegalAccessException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public static Fabric3Runtime<HostInfo> createRuntime(HostInfo hostInfo,
                                                         ClassLoader hostClassLoader,
                                                         ClassLoader bootClassLoader,
                                                         MonitorFactory monitorFactory) throws InitializationException {
        try {
            Class<?> implClass = Class.forName(RUNTIME_CLASS, true, bootClassLoader);
            Constructor<?> ctor = implClass.getConstructor(MonitorFactory.class);
            Fabric3Runtime<HostInfo> runtime = (Fabric3Runtime<HostInfo>) ctor.newInstance(monitorFactory);
            runtime.setHostClassLoader(hostClassLoader);
            runtime.setHostInfo(hostInfo);
            return runtime;
        } catch (IllegalAccessException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(e);
        } catch (NoSuchMethodException e) {
            throw new InitializationException(e);
        } catch (InvocationTargetException e) {
            throw new InitializationException(e);
        }
    }

    public static Bootstrapper createBootstrapper(HostInfo hostInfo, ClassLoader bootClassLoader) throws InitializationException {
        try {
            Class<?> implClass = Class.forName(BOOTSTRAPPER_CLASS, true, bootClassLoader);
            ScdlBootstrapper bootstrapper = (ScdlBootstrapper) implClass.newInstance();

            // set the system SCDL location
            File configDir = hostInfo.getConfigDirectory();
            URL systemSCDL = new File(configDir, "system.composite").toURI().toURL();
            bootstrapper.setScdlLocation(systemSCDL);

            // set the system configuration
            File systemConfig = new File(hostInfo.getModeConfigDirectory(), "systemConfig.xml");
            if (systemConfig.exists()) {
                bootstrapper.setSystemConfig(systemConfig.toURI().toURL());
            }
            return bootstrapper;
        } catch (IllegalAccessException e) {
            throw new InitializationException(e);
        } catch (MalformedURLException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(e);
        }

    }

    @SuppressWarnings({"unchecked"})
    public static RuntimeCoordinator createCoordinator(ClassLoader bootClassLoader) throws InitializationException {
        try {
            Class<?> implClass = Class.forName(COORDINATOR_CLASS, true, bootClassLoader);
            return (RuntimeCoordinator) implClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new InitializationException(e);
        } catch (IllegalAccessException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        }
    }

}
