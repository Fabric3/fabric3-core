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
package org.fabric3.admin.interpreter.command;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.admin.interpreter.communication.CommunicationException;
import org.fabric3.admin.interpreter.communication.DomainConnection;

/**
 *
 */
public class InstallCommand implements Command {
    private DomainConnection domainConnection;
    private URL contribution;
    private URI contributionUri;
    private String username;
    private String password;

    public InstallCommand(DomainConnection domainConnection) {
        this.domainConnection = domainConnection;
    }

    public void setContribution(URL contribution) {
        this.contribution = contribution;
    }

    public void setContributionUri(URI uri) {
        this.contributionUri = uri;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean execute(PrintStream out) throws CommandException {
        if (username != null) {
            domainConnection.setUsername(username);
        }
        if (password != null) {
            domainConnection.setPassword(password);
        }
        if (contributionUri == null) {
            contributionUri = CommandHelper.parseContributionName(contribution);
        }
        HttpURLConnection connection = null;
        try {
            String path = "/domain/contributions/contribution/" + contributionUri;
            connection = domainConnection.put(path, contribution);
            int code = connection.getResponseCode();
            if (HttpStatus.UNAUTHORIZED.getCode() == code) {
                out.println("ERROR: Not authorized");
                return false;
            } else if (HttpStatus.FORBIDDEN.getCode() == code && "http".equals(connection.getURL().getProtocol())) {
                out.println("An attempt was made to connect using HTTP but the domain requires HTTPS.");
                return false;
            } else if (HttpStatus.CONFLICT.getCode() == code) {
                out.println("The contribution is already installed: " + contributionUri);
                return false;
            } else if (HttpStatus.VALIDATION_ERROR.getCode() == code) {
                InputStream stream = connection.getErrorStream();
                Map<String, List<String>> errors = domainConnection.parse(Map.class, stream);
                out.println("ERROR: The contribution contains errors");
                printErrors(errors, out);
                return false;
            } else if (HttpStatus.CREATED.getCode() != code) {
                out.println("Error installing contribution: " + code);
                return false;
            } else {
                out.println("Installed " + contributionUri);
                return true;
            }
        } catch (FileNotFoundException e) {
            out.println("ERROR: File not found:" + e.getMessage());
            return false;
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain");
            e.printStackTrace(out);
            return false;
        } catch (IOException e) {
            out.println("ERROR: Error connecting to domain");
            e.printStackTrace(out);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Prints response errors to the given output stream.
     *
     * @param errors the response errors
     * @param out    the stream
     */
    public void printErrors(Map<String, List<String>> errors, PrintStream out) {
        for (Map.Entry<String, List<String>> entry : errors.entrySet()) {
            out.println("\nErrors in " + entry.getKey() + " \n");
            for (String message : entry.getValue()) {
                out.println("  " + message + "\n");
            }
        }

    }


}