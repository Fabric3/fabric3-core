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

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.management.domain.ComponentInfo;
import org.fabric3.management.domain.InvalidPathException;

/**
 * @version $Rev$ $Date$
 */
public class ListCommand implements Command {
    private DomainController controller;
    private String path;
    private String username;
    private String password;

    public ListCommand(DomainController controller) {
        this.controller = controller;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPath(String path) {
        this.path = path;
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
            try {
                List<ComponentInfo> infos = controller.getDeployedComponents(path);
                if (infos.isEmpty()) {
                    out.println("No components found");
                    return true;
                }
                out.println("Deployed components (" + path + "):");
                for (ComponentInfo info : infos) {
                    URI uri = info.getUri();
                    URI contributionUri = info.getContributionUri();
                    String zone = info.getZone();
                    out.println("   " + uri + " [Contribution: " + contributionUri + ", Zone: " + zone + "]");
                }
                return true;
            } catch (CommunicationException e) {
                out.println("ERROR: Error connecting to domain controller");
                e.printStackTrace(out);
                return false;
            } catch (InvalidPathException e) {
                out.println("Path was invalid: " + e.getMessage());
                return false;

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


}