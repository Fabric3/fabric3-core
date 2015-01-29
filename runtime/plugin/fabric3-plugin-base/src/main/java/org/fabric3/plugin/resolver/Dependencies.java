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

    public static final String F3_GROUP_ID = "org.fabric3";

    /**
     * Returns the core runtime extensions as a set of dependencies
     *
     * @param runtimeVersion the Fabric3 runtime version
     * @return the extensions
     */
    public static Set<Artifact> getCoreExtensions(String runtimeVersion) {
        Set<Artifact> extensions = new HashSet<>();

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


        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-resource", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-execution", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-plugin-extension", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-junit", "jar", runtimeVersion);
        extensions.add(artifact);

        artifact = new DefaultArtifact(F3_GROUP_ID, "fabric3-node-impl", "jar", runtimeVersion);
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
        Set<Artifact> artifacts = new HashSet<>();
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
