/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
import java.net.URL;
import javax.xml.namespace.QName;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.runtime.maven.MavenRuntime;

/**
 * Deploys a test composite.
 *
 * @version $Rev$ $Date$
 */
public class TestDeployer {
    private Log log;
    private String compositeNamespace;
    private String compositeName;
    private File buildDirectory;

    public TestDeployer(String compositeNamespace, String compositeName, File buildDirectory, Log log) {
        this.log = log;
        this.compositeNamespace = compositeNamespace;
        this.compositeName = compositeName;
        this.buildDirectory = buildDirectory;
    }

    public void deploy(MavenRuntime runtime) throws MojoExecutionException {
        try {
            QName qName = new QName(compositeNamespace, compositeName);
            log.info("Deploying test composite " + qName);
            URL buildDirUrl = getBuildDirectoryUrl();
            runtime.deploy(buildDirUrl, qName);
            runtime.startContext(qName);
        } catch (ValidationException e) {
            // print out the validation errors
            reportContributionErrors(e);
            String msg = "Contribution errors were found";
            throw new MojoExecutionException(msg);
        } catch (AssemblyException e) {
            reportDeploymentErrors(e);
            String msg = "Deployment errors were found";
            throw new MojoExecutionException(msg);
        } catch (Exception e) {
            // trap any other exception
            throw new MojoExecutionException("Error deploying test composite", e);
        }
    }

    private void reportContributionErrors(ValidationException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("CONTRIBUTION ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        log.error(b);
    }

    private void reportDeploymentErrors(AssemblyException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("DEPLOYMENT ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        log.error(b);
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
