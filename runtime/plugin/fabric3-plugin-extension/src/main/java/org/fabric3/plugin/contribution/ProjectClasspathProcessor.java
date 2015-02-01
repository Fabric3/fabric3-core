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
 */
package org.fabric3.plugin.contribution;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.plugin.api.runtime.PluginHostInfo;
import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.os.Library;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Fabricates a classpath for a Gradle project by including the main and test directories and any module dependencies.
 */
// FIXME merge with Maven deploy functionality into common superclass
@EagerInit
public class ProjectClasspathProcessor implements ClasspathProcessor {
    public static final String CONTENT_TYPE = "application/vnd.fabric3.plugin-project";
    private ClasspathProcessorRegistry registry;
    private PluginHostInfo hostInfo;

    public ProjectClasspathProcessor(@Reference ClasspathProcessorRegistry registry, @Reference PluginHostInfo hostInfo) {
        this.registry = registry;
        this.hostInfo = hostInfo;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public boolean canProcess(URL url) {
        if ("file".equals(url.getProtocol())) {
            // assume exploded directories are projects
            return true;
        }
        try {
            URLConnection conn = url.openConnection();
            return CONTENT_TYPE.equals(conn.getContentType());
        } catch (IOException e) {
            return false;
        }
    }

    public List<URL> process(URL url, List<Library> libraries) throws Fabric3Exception {
        try {
            List<URL> urls = new ArrayList<>(2);

            File classesDir = hostInfo.getClassesDir();
            File testDir = hostInfo.getTestClassesDir();

            urls.add(classesDir.toURI().toURL());
            urls.add(testDir.toURI().toURL());

            urls.addAll(hostInfo.getDependencyUrls());

            //add jars in META-INF/lib to classpath
            File metaInf = new File(classesDir, "META-INF");
            File metaInfLib = new File(metaInf, "lib");

            if (metaInfLib.exists()) {
                File[] jars = metaInfLib.listFiles(pathname -> pathname.getName().endsWith(".jar"));
                for (File jar : jars) {
                    urls.add(jar.toURI().toURL());
                }

            }
            return urls;
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }
}