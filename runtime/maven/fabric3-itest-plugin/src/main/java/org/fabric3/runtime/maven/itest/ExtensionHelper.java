/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;

import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;

/**
 * @version $Rev$ $Date$
 */
public class ExtensionHelper {
    public ArtifactHelper artifactHelper;

    public List<ContributionSource> processExtensions(Set<Dependency> extensions, Set<ArtifactRepository> repositories)
            throws MojoExecutionException {
        Set<URL> extensionUrls = resolveDependencies(extensions, repositories);
        return createContributionSources(extensionUrls);
    }

    private Set<URL> resolveDependencies(Set<Dependency> dependencies, Set<ArtifactRepository> repositories) throws MojoExecutionException {
        Set<URL> urls = new HashSet<URL>();
        if (dependencies != null) {
            for (Dependency dependency : dependencies) {
                Artifact artifact = artifactHelper.resolve(dependency, repositories);
                try {
                    urls.add(artifact.getFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new AssertionError();
                }
            }
        }
        return urls;
    }

    private List<ContributionSource> createContributionSources(Set<URL> urls) {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        for (URL extensionUrl : urls) {
            // it's ok to assume archives are uniquely named since most server environments have a single deploy directory
            URI uri = URI.create(new File(extensionUrl.getFile()).getName());
            ContributionSource source = new FileContributionSource(uri, extensionUrl, -1, true);
            sources.add(source);
        }
        return sources;
    }

}
