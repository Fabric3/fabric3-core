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
package org.fabric3.runtime.maven.itest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Dependency;

/**
 *
 */
public class Dependencies {
    public static final String F3_GORUP_ID = "org.codehaus.fabric3";

    /**
     * Returns the core runtime extensions as a set of dependencies
     *
     * @param runtimeVersion the Fabric3 runtime version
     * @return the extensions
     */
    public static Set<Dependency> getCoreExtensions(String runtimeVersion) {
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

    /**
     * Returns the main maven host module.
     *
     * @param runtimeVersion the Fabric3 runtime version
     * @return the main maven host module
     */
    public static Dependency getMainRuntimeModule(String runtimeVersion) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(F3_GORUP_ID);
        dependency.setArtifactId("fabric3-maven3-host");
        dependency.setVersion(runtimeVersion);
        return dependency;
    }

    /**
     * Returns the host modules as a set of dependencies
     *
     * @param runtimeVersion the Fabric3 runtime version
     * @return the extensions
     */
    public static List<Dependency> getHostDependencies(String runtimeVersion) {
        List<Dependency> dependencies = new ArrayList<>();
        Dependency fabric3Api = new Dependency();
        fabric3Api.setGroupId(F3_GORUP_ID);
        fabric3Api.setArtifactId("fabric3-api");
        fabric3Api.setVersion(runtimeVersion);
        dependencies.add(fabric3Api);

        // add commons annotations dependency
        Dependency jsr250API = new Dependency();
        jsr250API.setGroupId("javax.annotation");
        jsr250API.setArtifactId("javax.annotation-api");
        jsr250API.setVersion("1.2");
        dependencies.add(jsr250API);

        // add JAXB API dependency
        Dependency jaxbAPI = new Dependency();
        jaxbAPI.setGroupId("javax.xml.bind");
        jaxbAPI.setArtifactId("jaxb-api-osgi");
        jaxbAPI.setVersion("2.2-promoted-b50");
        dependencies.add(jaxbAPI);

        // add JAX-RS API
        Dependency rsAPI = new Dependency();
        rsAPI.setGroupId("javax.ws.rs");
        rsAPI.setArtifactId("javax.ws.rs-api");
        rsAPI.setVersion("2.0");
        dependencies.add(rsAPI);

        // add Node API
        Dependency nodeAPI = new Dependency();
        nodeAPI.setGroupId(F3_GORUP_ID);
        nodeAPI.setArtifactId("fabric3-node-api");
        nodeAPI.setVersion(runtimeVersion);
        dependencies.add(nodeAPI);

        // add JUnit API
        Dependency junitAPI = new Dependency();
        junitAPI.setGroupId(F3_GORUP_ID);
        junitAPI.setArtifactId("fabric3-junit-api");
        junitAPI.setVersion(runtimeVersion);
        dependencies.add(junitAPI);

        return dependencies;
    }

}
