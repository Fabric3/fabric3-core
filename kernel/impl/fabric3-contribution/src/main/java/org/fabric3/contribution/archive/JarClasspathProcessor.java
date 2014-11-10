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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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
        List<URL> classpath = new ArrayList<>();
        // add the the jar itself to the classpath
        classpath.add(jar);

        if (libraries.isEmpty() && !hasLibDirectory(new File(jar.getFile()))) {
            return classpath;
        }

        File dir = hostInfo.getTempDir();
        Set<String> resolvedLibraryPaths = resolveNativeLibraries(libraries);
        try (InputStream is = jar.openStream()) {

            JarInputStream jarStream = new JarInputStream(is);
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                int index = name.lastIndexOf("/");
                String path;
                if (index > 0) {
                    path = name.substring(0, index);
                } else {
                    path = name;
                }

                if (resolvedLibraryPaths.contains(name)) {
                    extractNativeLibrary(path, jarStream, entry);
                    continue;
                }
                if (!path.startsWith("META-INF/lib") || (path.startsWith("META-INF/lib") && name.length() <= 13)) {
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
                    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile))) {
                        IOHelper.copy(jarStream, os);
                        os.flush();
                    }
                    jarFile.deleteOnExit();
                    classpath.add(jarFile.toURI().toURL());
                }
            }
            return classpath;
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
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(library))) {
            IOHelper.copy(jarStream, os);
            os.flush();
        }
        library.deleteOnExit();
    }

    private void explodeJar(File dir, JarInputStream jarStream, File explodedDirectory) throws IOException {

        if (!explodedDirectory.exists()) {

            explodedDirectory.mkdirs();
            File jarFile = File.createTempFile("fabric3", ".jar", dir);
            jarFile.createNewFile();

            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile))) {
                IOHelper.copy(jarStream, os);
                os.flush();
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
        Set<String> paths = new HashSet<>();
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
