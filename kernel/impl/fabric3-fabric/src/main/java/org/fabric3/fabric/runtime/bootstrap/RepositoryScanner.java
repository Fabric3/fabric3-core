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
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.FileContributionSource;
import org.fabric3.api.host.contribution.SyntheticContributionSource;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.ScanException;
import org.fabric3.api.host.runtime.ScanResult;

/**
 * Scans a repository for extension and user contributions.
 */
public class RepositoryScanner {

    /**
     * Scans a repository directory for contributions.
     *
     * @param info the host info
     * @return the contributions grouped by user and extension contributions
     * @throws ScanException if there is an error scanning teh directory
     */
    public ScanResult scan(HostInfo info) throws ScanException {
        List<ContributionSource> extensionSources = scan(info.getExtensionsRepositoryDirectory(), true);
        List<ContributionSource> runtimeSources = scan(info.getRuntimeRepositoryDirectory(), true);
        extensionSources.addAll(runtimeSources);
        List<ContributionSource> userSource = scan(info.getUserRepositoryDirectory(), false);
        return new ScanResult(extensionSources, userSource);
    }

    private List<ContributionSource> scan(File directory, boolean extension) throws ScanException {
        List<ContributionSource> sources = new ArrayList<>();
        if (directory == null) {
            return sources;
        }
        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                // skip directories and files beginning with '.'
                return !pathname.getName().startsWith(".");
            }
        });
        if (files != null) {
            for (File file : files) {
                try {
                    URL location = file.toURI().toURL();
                    ContributionSource source;
                    if (file.isDirectory()) {
                        // create synthetic contributions from directories contained in the repository
                        URI uri = URI.create("f3-" + file.getName());
                        source = new SyntheticContributionSource(uri, location, extension);

                    } else {
                        URI uri = URI.create(file.getName());
                        source = new FileContributionSource(uri, location, -1, extension);
                    }
                    sources.add(source);
                } catch (MalformedURLException e) {
                    throw new ScanException("Error loading contribution:" + file.getName(), e);
                }
            }
        }
        return sources;
    }

}
