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
package org.fabric3.admin.interpreter.command;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.management.contribution.ContributionManagementException;
import org.fabric3.management.contribution.DuplicateContributionManagementException;
import org.fabric3.management.contribution.InvalidContributionException;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.InvalidDeploymentException;

/**
 * @version $Rev$ $Date$
 */
public class DeployCommand implements Command {
    private DomainController controller;
    private URI contributionUri;
    private String username;
    private String password;
    private String planName;
    private URL planFile;

    public DeployCommand(DomainController controller) {
        this.controller = controller;
    }

    public URI getContributionUri() {
        return contributionUri;
    }

    public void setContributionUri(URI uri) {
        this.contributionUri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPlanName(String plan) {
        this.planName = plan;
    }

    public void setPlanFile(URL planFile) {
        this.planFile = planFile;
    }

    public boolean execute(PrintStream out) throws CommandException {
        if (username != null) {
            controller.setUsername(username);
        }
        if (password != null) {
            controller.setPassword(password);
        }
        boolean disconnected = !controller.isConnected();
        try {
            if (disconnected) {
                try {
                    controller.connect();
                } catch (IOException e) {
                    out.println("ERROR: Error connecting to domain controller");
                    e.printStackTrace(out);
                    return false;
                }
            }
            if (planName != null) {
                return deployByName(out);
            } else if (planFile != null) {
                return deployByFile(out);
            } else {
                return deployNoPlan(out);
            }
        } finally {
            if (disconnected && controller.isConnected()) {
                try {
                    controller.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean deployByName(PrintStream out) {
        try {
            controller.deploy(contributionUri, planName);
            out.println("Deployed " + contributionUri);
            return true;
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain controller");
            e.printStackTrace(out);
        } catch (InvalidDeploymentException e) {
            out.println("The following deployment errors were reported:");
            for (String desc : e.getErrors()) {
                out.println("ERROR: " + desc);
            }
        } catch (DeploymentManagementException e) {
            out.println("ERROR: Error deploying contribution");
            out.println("       " + e.getMessage());
        }
        return false;
    }

    private boolean deployByFile(PrintStream out) {
        URI planContributionUri = CommandHelper.parseContributionName(planFile);
        try {
            // store and install plan
            controller.store(planFile, planContributionUri);
            controller.install(planContributionUri);
            String installedPlanName = parsePlanName();
            controller.deploy(contributionUri, installedPlanName);
            out.println("Deployed " + contributionUri);
            return true;
        } catch (InvalidDeploymentException e) {
            out.println("The following deployment errors were reported:");
            for (String desc : e.getErrors()) {
                out.println("ERROR: " + desc);
            }
        } catch (DeploymentManagementException e) {
            out.println("ERROR: Error deploying contribution");
            out.println("       " + e.getMessage());
            revertPlan(planContributionUri, out);
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain controller");
            e.printStackTrace(out);
        } catch (InvalidContributionException e) {
            out.println("The following errors were found in the deployment plan:\n");
            CommandHelper.printErrors(out, e);
            revertPlan(planContributionUri, out);
        } catch (DuplicateContributionManagementException e) {
            out.println("ERROR: Deployment plan already exists");
        } catch (ContributionManagementException e) {
            out.println("ERROR: There was a problem installing the deployment plan: " + planFile);
            out.println("       " + e.getMessage());
            revertPlan(planContributionUri, out);
        } catch (IOException e) {
            out.println("ERROR: Unable to read deployment plan: " + planFile);
            e.printStackTrace(out);
        } catch (ParserConfigurationException e) {
            out.println("ERROR: Unable to read deployment plan: " + planFile);
            e.printStackTrace(out);
        } catch (SAXException e) {
            out.println("ERROR: Unable to read deployment plan: " + planFile);
            e.printStackTrace(out);
        }
        return false;
    }

    private boolean deployNoPlan(PrintStream out) {
        try {
            controller.deploy(contributionUri);
            out.println("Deployed " + contributionUri);
            return true;
        } catch (InvalidDeploymentException e) {
            out.println("The following deployment errors were reported:");
            for (String desc : e.getErrors()) {
                out.println("ERROR: " + desc);
            }
        } catch (DeploymentManagementException e) {
            out.println("ERROR: Error deploying contribution");
            out.println("       " + e.getMessage());
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain controller");
            e.printStackTrace(out);
        }
        return false;
    }

    private String parsePlanName() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = f.newDocumentBuilder();
        Document d = b.parse(planFile.openStream());
        return d.getDocumentElement().getAttribute("name");
    }

    private void revertPlan(URI planContributionUri, PrintStream out) {
        // remove the plan from the persistent store
        try {
            controller.uninstall(planContributionUri);
            controller.remove(planContributionUri);
        } catch (CommunicationException ex) {
            out.println("ERROR: Error connecting to domain controller");
            ex.printStackTrace(out);
        } catch (ContributionManagementException ex) {
            out.println("ERROR: Error reverting deployment plan");
            out.println("       " + ex.getMessage());
        }
    }


}