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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.host.runtime;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import org.fabric3.host.contribution.ContributionSource;

/**
 * Encapsulates configuration needed to bootstrap a runtime.
 *
 * @version $Rev$ $Date$
 */
public class BootConfiguration {
    private Fabric3Runtime runtime;
    private URL systemCompositeUrl;
    private Document systemConfig;
    private ClassLoader bootClassLoader;
    private Map<String, String> exportedPackages = new HashMap<String, String>();
    private List<ComponentRegistration> registrations = new ArrayList<ComponentRegistration>();
    private List<ContributionSource> extensionContributions = Collections.emptyList();
    private List<ContributionSource> userContributions = Collections.emptyList();
    private ClassLoader hostClassLoader;

    public Fabric3Runtime getRuntime() {
        return runtime;
    }

    public void setRuntime(Fabric3Runtime runtime) {
        this.runtime = runtime;
    }

    public URL getSystemCompositeUrl() {
        return systemCompositeUrl;
    }

    public void setSystemCompositeUrl(URL url) {
        this.systemCompositeUrl = url;
    }

    public Document getSystemConfig() {
        return systemConfig;
    }

    public void setSystemConfig(Document systemConfig) {
        this.systemConfig = systemConfig;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setBootClassLoader(ClassLoader bootClassLoader) {
        this.bootClassLoader = bootClassLoader;
    }

    public ClassLoader getHostClassLoader() {
        return hostClassLoader;
    }

    public void setHostClassLoader(ClassLoader hostClassLoader) {
        this.hostClassLoader = hostClassLoader;
    }

    public Map<String, String> getExportedPackages() {
        return exportedPackages;
    }

    public void setExportedPackages(Map<String, String> exportedPackages) {
        this.exportedPackages = exportedPackages;
    }

    public List<ComponentRegistration> getRegistrations() {
        return registrations;
    }

    public void addRegistrations(List<ComponentRegistration> registrations) {
        this.registrations.addAll(registrations);
    }

    public List<ContributionSource> getExtensionContributions() {
        return extensionContributions;
    }

    public void setExtensionContributions(List<ContributionSource> extensionContributions) {
        this.extensionContributions = extensionContributions;
    }

    public List<ContributionSource> getUserContributions() {
        return userContributions;
    }

    public void setUserContributions(List<ContributionSource> userContributions) {
        this.userContributions = userContributions;
    }

}
