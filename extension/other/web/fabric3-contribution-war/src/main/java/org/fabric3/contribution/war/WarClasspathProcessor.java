/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.contribution.war;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.contribution.Library;
import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;

/**
 * Creates a classpath based on the contents of a WAR. Specifically, adds jars contained in WEB-INF/lib and classes in WEB-INF/classes to the
 * classpath.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class WarClasspathProcessor implements ClasspathProcessor {
    private static final Random RANDOM = new Random();

    private final ClasspathProcessorRegistry registry;
    private HostInfo info;

    public WarClasspathProcessor(@Reference ClasspathProcessorRegistry registry, @Reference HostInfo info) {
        this.registry = registry;
        this.info = info;
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
        return name.endsWith(".war");
    }

    public List<URL> process(URL url, List<Library> libraries) throws IOException {
        List<URL> classpath = new ArrayList<URL>();
        // add the the jar itself to the classpath
        classpath.add(url);

        // add libraries from the jar
        addLibraries(classpath, url);
        return classpath;
    }

    private void addLibraries(List<URL> classpath, URL jar) throws IOException {
        File dir = info.getTempDir();
        InputStream is = jar.openStream();
        try {
            JarInputStream jarStream = new JarInputStream(is);
            JarEntry entry;
            File classesDir = null;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String path = entry.getName();
                if (path.startsWith("WEB-INF/lib/")) {
                    // expand jars in WEB-INF/lib and add to the classpath
                    File jarFile = File.createTempFile("fabric3", ".jar", dir);
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile));
                    try {
                        copy(jarStream, os);
                        os.flush();
                    } finally {
                        os.close();
                    }
                    jarFile.deleteOnExit();
                    classpath.add(jarFile.toURI().toURL());
                } else if (path.startsWith("WEB-INF/classes/")) {
                    // expand classes in WEB-INF/classes and add to classpath
                    if (classesDir == null) {
                        classesDir = new File(dir, "webclasses" + RANDOM.nextInt());
                        classesDir.mkdir();
                        classesDir.deleteOnExit();
                    }
                    int lastDelimeter = path.lastIndexOf("/");
                    String name = path.substring(lastDelimeter);
                    File pathAndPackageName;
                    if (lastDelimeter < 16) { // 16 is length of "WEB-INF/classes
                        // in case there is no trailing '/', i.e. properties files or other resources under WEB_INF/classes
                        pathAndPackageName = classesDir;
                    } else {
                        pathAndPackageName = new File(classesDir, path.substring(16, lastDelimeter));
                    }
                    pathAndPackageName.mkdirs();
                    pathAndPackageName.deleteOnExit();
                    File classFile = new File(pathAndPackageName, name);
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(classFile));
                    try {
                        copy(jarStream, os);
                        os.flush();
                    } finally {
                        os.close();
                    }
                    classFile.deleteOnExit();
                    classpath.add(classesDir.toURI().toURL());
                }
            }
        } finally {
            is.close();
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

}