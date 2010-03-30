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
package org.fabric3.admin.interpreter.command;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.management.contribution.ContributionManagementException;
import org.fabric3.management.contribution.ContributionRemoveException;
import org.fabric3.management.contribution.DuplicateContributionManagementException;
import org.fabric3.management.contribution.InvalidContributionException;

/**
 * @version $Rev$ $Date$
 */
public class InstallCommand implements Command {
    private DomainController controller;
    private URL contribution;
    private URI contributionUri;
    private String username;
    private String password;

    public InstallCommand(DomainController controller) {
        this.controller = controller;
    }

    public URL getContribution() {
        return contribution;
    }

    public void setContribution(URL contribution) {
        this.contribution = contribution;
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

    public boolean execute(PrintStream out) throws CommandException {
        boolean disconnected = !controller.isConnected();
        try {
            if (username != null) {
                controller.setUsername(username);
            }
            if (password != null) {
                controller.setPassword(password);
            }
            if (disconnected) {
                controller.connect();
            }
            if (contributionUri == null) {
                contributionUri = CommandHelper.parseContributionName(contribution);
            }
            controller.store(contribution, contributionUri);
            controller.install(contributionUri);
            out.println("Installed " + contributionUri);
            return true;
        } catch (DuplicateContributionManagementException e) {
            out.println("ERROR: A contribution with that name already exists");
        } catch (CommunicationException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                out.println("ERROR: File not found:" + e.getMessage());
                return false;
            }
            throw new CommandException(e);
        } catch (IOException e) {
            out.println("ERROR: Unable to connect to the domain controller");
            e.printStackTrace(out);
        } catch (InvalidContributionException e) {
            out.println("ERROR: " + e.getMessage());
            CommandHelper.printErrors(out, e);
            try {
                controller.remove(contributionUri);
            } catch (CommunicationException ex) {
                out.println("Unable to remove contribution due to a communication error:");
                ex.printStackTrace(out);
            } catch (ContributionRemoveException ex) {
                out.println("Error removing conribution:");
                ex.printStackTrace(out);
            }

        } catch (ContributionManagementException e) {
            out.println("ERROR: " + e.getMessage());
            try {
                controller.remove(contributionUri);
            } catch (CommunicationException ex) {
                System.out.println("Unable to remove contribution due to a communication error:");
                ex.printStackTrace();
            } catch (ContributionRemoveException ex) {
                System.out.println("Error removing conribution:");
                ex.printStackTrace();
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
        return false;
    }


}