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
package org.fabric3.runtime.maven.contribution;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.runtime.maven.MavenHostInfo;
import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.os.Library;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Fabricates a classpath for a Maven module by including the classes and test-classes directories and any module dependencies.
 */
@EagerInit
public class ModuleClasspathProcessor implements ClasspathProcessor {
    public static final String CONTENT_TYPE = "application/vnd.fabric3.maven-project";
    private ClasspathProcessorRegistry registry;
    private MavenHostInfo hostInfo;

    public ModuleClasspathProcessor(@Reference ClasspathProcessorRegistry registry, @Reference MavenHostInfo hostInfo) {
        this.registry = registry;
        this.hostInfo = hostInfo;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public boolean canProcess(URL url) {
        if ("file".equals(url.getProtocol())) {
            // assume exploded directories are maven modules
            return true;
        }
        try {
            URLConnection conn = url.openConnection();
            return CONTENT_TYPE.equals(conn.getContentType());
        } catch (IOException e) {
            return false;
        }
    }

    public List<URL> process(URL url, List<Library> libraries) throws IOException {
        String file = url.getFile();
        final List<URL> urls = new ArrayList<URL>(2);
        File classesDir = new File(file, "classes");
        urls.add(classesDir.toURI().toURL());
        File testClassesDir = new File(file, "test-classes");
        urls.add(testClassesDir.toURI().toURL());
        urls.addAll(hostInfo.getDependencyUrls());

        //add jars in META-INF/lib to classpath
        File metaInf = new File(classesDir, "META-INF");
        File metaInfLib = new File(metaInf, "lib");

        if (metaInfLib.exists()) {
            File[] jars = metaInfLib.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar");
                }
            });
            for (File jar : jars) {
                urls.add(jar.toURI().toURL());
            }

        }
        return urls;
    }
}
