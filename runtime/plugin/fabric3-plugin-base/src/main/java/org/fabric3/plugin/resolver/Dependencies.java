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
package org.fabric3.plugin.resolver;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.fabric3.plugin.runtime.PluginConstants;

/**
 * Details plugin runtime module dependencies.
 */
public class Dependencies {

    public static final String F3_GROUP_ID = "org.codehaus.fabric3";

    /**
     * Returns the core runtime extensions as a set of dependencies
     *
     * @param runtimeVersion the Fabric3 runtime version
     * @return the extensions
     */
    public static Set<Artifact> getCoreExtensions(String runtimeVersion) {
        Set<Artifact> extensions = new HashSet<Artifact>();

        Artifact artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-jdk-proxy", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-test-spi", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-channel-impl", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-java", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-async", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-sca-intents", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-resource", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-execution", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-plugin-extension", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-junit", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact("junit", "junit", "jar", PluginConstants.JUNIT_VERSION);
        extensions.add(artifact);

        return extensions;
    }

    /**
     * Returns the main maven host module.
     *
     * @param runtimeVersion the Fabric3 runtime version
     * @return the main maven host module
     */
    public static Artifact getMainRuntimeModule(String runtimeVersion) {
        return new DefaultArtifact(F3_GROUP_ID, "fabric3-plugin-runtime", "jar", runtimeVersion);
    }

    /**
     * Returns the host modules as a set of dependencies
     *
     * @param runtimeVersion the Fabric3 runtime version
     * @return the extensions
     */
    public static Set<Artifact> getHostDependencies(String runtimeVersion) {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        Artifact fabric3Api = new DefaultArtifact(F3_GROUP_ID, "fabric3-api", "jar", runtimeVersion);
        artifacts.add(fabric3Api);

        // add commons annotations dependency
        Artifact jsr250API = new DefaultArtifact("javax.annotation", "javax.annotation-api", "jar", "1.2");
        artifacts.add(jsr250API);

        // add JAXB API dependency
        Artifact jaxbAPI = new DefaultArtifact("javax.xml.bind", "jaxb-api-osgi", "jar", "2.2-promoted-b50");
        artifacts.add(jaxbAPI);

        // add JAX-RS API
        Artifact rsAPI = new DefaultArtifact("javax.ws.rs", "javax.ws.rs-api", "jar", "2.0");
        artifacts.add(rsAPI);

        // add Node API
        Artifact nodeAPI = new DefaultArtifact(F3_GROUP_ID, "fabric3-node-api", "jar", runtimeVersion);
        artifacts.add(nodeAPI);

        // add JUnit API
        Artifact junitAPI = new DefaultArtifact(F3_GROUP_ID, "fabric3-junit-api", "jar", runtimeVersion);
        artifacts.add(junitAPI);

        return artifacts;
    }

}
