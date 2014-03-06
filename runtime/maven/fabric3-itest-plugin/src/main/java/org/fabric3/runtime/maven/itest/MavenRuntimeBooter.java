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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.fabric3.host.Names;
import org.fabric3.host.os.OperatingSystem;
import org.fabric3.host.runtime.BootstrapHelper;
import org.fabric3.plugin.api.runtime.PluginHostInfo;
import org.fabric3.plugin.runtime.AbstractPluginRuntimeBooter;
import org.fabric3.plugin.runtime.PluginBootConfiguration;
import org.fabric3.plugin.runtime.PluginConstants;
import org.fabric3.plugin.runtime.PluginHostInfoImpl;

/**
 * Boots the plugin runtime in a Maven process.
 */
public class MavenRuntimeBooter extends AbstractPluginRuntimeBooter {
    private static final String PLUGIN_RUNTIME_IMPL = "org.fabric3.plugin.runtime.impl.PluginRuntimeImpl";

    public MavenRuntimeBooter(PluginBootConfiguration configuration) {
        super(configuration);
    }

    protected String getPluginClass() {
        return PLUGIN_RUNTIME_IMPL;
    }

    protected Map<String, String> getExportedPackages() {
        Map<String, String> exportedPackages = new HashMap<String, String>();
        exportedPackages.put("org.fabric3.runtime.maven", Names.VERSION);
        return exportedPackages;
    }

    protected PluginHostInfo createHostInfo(String environment, Set<URL> moduleDependencies, File outputDirectory, File buildDir) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), ".f3");

        URI domain = URI.create(PluginConstants.DOMAIN);
        File baseDir = new File(outputDirectory, "test-classes");
        File classDir = new File(outputDirectory, "classes");
        OperatingSystem os = BootstrapHelper.getOperatingSystem();
        // Maven modules place resources and test resources in the respective compiled classes directories
        return new PluginHostInfoImpl(domain, environment, moduleDependencies, baseDir, tempDir, outputDirectory, classDir, classDir, baseDir, baseDir, os);

    }

}
