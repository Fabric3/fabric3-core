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
package org.fabric3.fabric.runtime.bootstrap;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.FileContributionSource;
import org.fabric3.api.host.contribution.SyntheticContributionSource;
import org.fabric3.api.host.runtime.HostInfo;

/**
 * Scans for configured extensions.
 */
public class ExtensionsScanner {

    /**
     * Scans the extensions directory for contributions.
     *
     * @param info the host info
     * @return the extension contributions
     * @throws Fabric3Exception if there is an error scanning the directory
     */
    public List<ContributionSource> scan(HostInfo info) throws Fabric3Exception {
        return scan(info.getExtensionsRepositoryDirectory(), true);
    }

    private List<ContributionSource> scan(File directory, boolean extension) throws Fabric3Exception {
        List<ContributionSource> sources = new ArrayList<>();
        if (directory == null) {
            return sources;
        }
        File[] files = directory.listFiles(pathname -> !pathname.getName().startsWith("."));
        if (files != null) {
            for (File file : files) {
                try {
                    URL location = file.toURI().toURL();
                    ContributionSource source;
                    if (file.isDirectory()) {
                        // create synthetic contributions from directories contained in the directory
                        URI uri = URI.create("f3-" + file.getName());
                        source = new SyntheticContributionSource(uri, location, extension);

                    } else {
                        URI uri = URI.create(file.getName());
                        source = new FileContributionSource(uri, location, -1, extension);
                    }
                    sources.add(source);
                } catch (MalformedURLException e) {
                    throw new Fabric3Exception("Error loading extension:" + file.getName(), e);
                }
            }
        }
        return sources;
    }

}
