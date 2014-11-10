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
package org.fabric3.plugin.deployer;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.api.host.contribution.ValidationException;
import org.fabric3.api.host.domain.AssemblyException;
import org.fabric3.plugin.Fabric3PluginException;
import org.fabric3.plugin.api.runtime.PluginRuntime;

/**
 * Base functionality for deploying a build module or projects as a contribution to the plugin runtime.
 */
public abstract class AbstractDeployer {
    private String compositeNamespace;
    private String compositeName;
    private File buildDirectory;

    public AbstractDeployer(String compositeNamespace, String compositeName, File buildDirectory) {
        this.compositeNamespace = compositeNamespace;
        this.compositeName = compositeName;
        this.buildDirectory = buildDirectory;
    }

    public boolean deploy(PluginRuntime runtime, String errorText) throws Fabric3PluginException {
        try {
            QName qName = new QName(compositeNamespace, compositeName);
            URL buildDirUrl = getBuildDirectoryUrl();
            runtime.deploy(buildDirUrl, qName);
            return true;
        } catch (ValidationException e) {
            if (errorText != null && e.getMessage() != null && e.getMessage().contains(errorText)) {
                return false;
            }
            // print out the validation errors
            reportContributionErrors(e);
            String msg = "Contribution errors were found";
            throw new Fabric3PluginException(msg);
        } catch (AssemblyException e) {
            if (errorText != null && e.getMessage().contains(errorText)) {
                return false;
            }
            reportDeploymentErrors(e);
            String msg = "Deployment errors were found";
            throw new Fabric3PluginException(msg);
        } catch (Exception e) {
            // trap any other exception
            if (errorText != null && e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().contains(errorText)) {
                return false;
            }
            throw new Fabric3PluginException("Error deploying test composite", e);
        }
    }

    protected abstract void logError(String message);

    private void reportContributionErrors(ValidationException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("CONTRIBUTION ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        logError(b.toString());
    }

    private void reportDeploymentErrors(AssemblyException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("DEPLOYMENT ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        logError(b.toString());
    }

    private URL getBuildDirectoryUrl() {
        try {
            return buildDirectory.toURI().toURL();
        } catch (MalformedURLException e) {
            // this should not happen
            throw new AssertionError();
        }
    }
}
