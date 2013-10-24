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
package org.fabric3.contribution.archive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.util.IOHelper;
import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.model.os.OperatingSystemSpec;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates a classpath based on the contents of a jar by adding the jar and any zip/jar archives found in META-INF/lib to the classpath. This is dome using one
 * of two strategies. If the <code>$systemConfig//runtime/explode.jars</code> property is set to false (the default), embedded jars will be copied to a
 * temporary file, which is placed on the classpath using a jar: URL. If set to true, the contents of the embedded jar file will be extracted to the filesystem
 * and placed on the classpath using a file: URL instead.
 * <p/>
 * The extract option is designed to work around a bug on Windows where the Sun JVM acquires an OS read lock on jar files when accessing resources from a jar:
 * URL and does not release it. This results in holding open temporary file handles and not being able to delete those files until the JVM terminates. This
 * issue does not occur on Unix systems.
 */
@EagerInit
public class JarClasspathProcessor implements ClasspathProcessor {
    private ClasspathProcessorRegistry registry;
    private HostInfo hostInfo;
    private boolean explodeJars;
    private File libraryDir;

    public JarClasspathProcessor(@Reference ClasspathProcessorRegistry registry, @Reference HostInfo hostInfo) {
        this.registry = registry;
        this.hostInfo = hostInfo;
        libraryDir = hostInfo.getNativeLibraryDir();
    }

    @Property(required = false)
    @Source("$systemConfig//f3:runtime/f3:explode.jars")
    public void setExplodeJars(boolean explodeJars) {
        this.explodeJars = explodeJars;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    @Destroy
    public void destroy() {
        registry.unregister(this);
    }

    public boolean canProcess(URL url) {
        String name = url.getFile().toLowerCase();
        return name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith("/classes") || name.endsWith("/classes/");
    }

    public List<URL> process(URL jar, List<Library> libraries) throws IOException {
        List<URL> classpath = new ArrayList<URL>();
        // add the the jar itself to the classpath
        classpath.add(jar);

        if (libraries.isEmpty() && !hasLibDirectory(new File(jar.getFile()))) {
            return classpath;
        }

        File dir = hostInfo.getTempDir();
        InputStream is = jar.openStream();
        Set<String> resolvedLibraryPaths = resolveNativeLibraries(libraries);
        try {

            JarInputStream jarStream = new JarInputStream(is);
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                int index = entry.getName().lastIndexOf("/");
                String path;
                if (index > 0) {
                    path = entry.getName().substring(0, index);
                } else {
                    path = entry.getName();
                }

                if (resolvedLibraryPaths.contains(entry.getName())) {
                    extractNativeLibrary(path, jarStream, entry);
                    continue;
                }
                if (!path.startsWith("META-INF/lib")) {
                    continue;
                }
                if (explodeJars) {
                    String fileName = path.substring(path.lastIndexOf('/'));
                    File explodedDirectory = new File(dir, fileName);
                    explodeJar(dir, jarStream, explodedDirectory);
                    classpath.add(explodedDirectory.toURI().toURL());
                } else {
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File jarFile = File.createTempFile("fabric3", ".jar", dir);
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile));
                    try {
                        IOHelper.copy(jarStream, os);
                        os.flush();
                    } finally {
                        os.close();
                    }
                    jarFile.deleteOnExit();
                    classpath.add(jarFile.toURI().toURL());
                }
            }
            return classpath;
        } finally {
            is.close();
        }
    }

    private void extractNativeLibrary(String path, JarInputStream jarStream, JarEntry entry) throws IOException {
        File dir = hostInfo.getTempDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!libraryDir.exists()) {
            libraryDir.mkdirs();
            libraryDir.deleteOnExit();
        }
        String libraryName = entry.getName().substring(path.length() + 1); // add '/' separator
        File library = new File(libraryDir, libraryName);
        if (library.exists()) {
            return;
        }
        OutputStream os = new BufferedOutputStream(new FileOutputStream(library));
        try {
            IOHelper.copy(jarStream, os);
            os.flush();
        } finally {
            os.close();
        }
        library.deleteOnExit();
    }

    private void explodeJar(File dir, JarInputStream jarStream, File explodedDirectory) throws IOException {

        if (!explodedDirectory.exists()) {

            explodedDirectory.mkdirs();
            File jarFile = File.createTempFile("fabric3", ".jar", dir);
            jarFile.createNewFile();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile));

            try {
                IOHelper.copy(jarStream, os);
                os.flush();
            } finally {
                os.close();
            }

            try {

                FileInputStream inputStream = new FileInputStream(jarFile);
                JarInputStream jarInputStream = new JarInputStream(inputStream);

                JarEntry entry;
                while ((entry = jarInputStream.getNextJarEntry()) != null) {

                    String filePath = entry.getName();
                    if (entry.isDirectory()) {
                        continue;
                    }

                    File entryFile = new File(explodedDirectory, filePath);
                    entryFile.getParentFile().mkdirs();

                    entryFile.createNewFile();
                    OutputStream entryOutputStream = new BufferedOutputStream(new FileOutputStream(entryFile));
                    IOHelper.copy(jarInputStream, entryOutputStream);
                    entryOutputStream.flush();
                    entryOutputStream.close();

                }

                inputStream.close();

            } finally {
                jarFile.delete();
            }
        }
    }

    private boolean hasLibDirectory(File file) {
        InputStream stream = null;
        try {
            URL jarUrl = new URL("jar:" + file.toURI().toURL().toExternalForm() + "!/META-INF/lib");
            stream = jarUrl.openStream();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                IOHelper.closeQuietly(stream);
            } catch (NullPointerException e) {
                // ignore will be thrown if the directory exists as the underlying stream is null
            }
        }
    }

    private Set<String> resolveNativeLibraries(List<Library> libraries) {
        Set<String> paths = new HashSet<String>();
        for (Library library : libraries) {
            for (OperatingSystemSpec os : library.getOperatingSystems()) {
                if (os.matches(hostInfo.getOperatingSystem())) {
                    paths.add(library.getPath());
                    break;
                }
            }
        }
        return paths;
    }

}
