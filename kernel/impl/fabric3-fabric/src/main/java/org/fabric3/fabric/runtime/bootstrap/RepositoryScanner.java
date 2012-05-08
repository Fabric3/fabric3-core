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
package org.fabric3.fabric.runtime.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.contribution.SyntheticContributionSource;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.ScanException;
import org.fabric3.host.runtime.ScanResult;

/**
 * Scans a repository for extension and user contributions.
 *
 * @version $Rev$ $Date$
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

    public List<ContributionSource> scan(File directory, boolean extension) throws ScanException {

        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                // skip directories and files beginning with '.'
                return !pathname.getName().startsWith(".");
            }
        });
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
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
