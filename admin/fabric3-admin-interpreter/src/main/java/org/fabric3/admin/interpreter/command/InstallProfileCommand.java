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
package org.fabric3.admin.interpreter.command;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.admin.interpreter.communication.CommunicationException;
import org.fabric3.admin.interpreter.communication.DomainConnection;

/**
 *
 */
public class InstallProfileCommand implements Command {
    private DomainConnection domainConnection;
    private URL profile;
    private URI profileUri;
    private String username;
    private String password;

    public InstallProfileCommand(DomainConnection domainConnection) {
        this.domainConnection = domainConnection;
    }

    public void setProfile(URL profile) {
        this.profile = profile;
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
        if (profileUri == null) {
            profileUri = CommandHelper.parseContributionName(profile);
        }
        storeProfile(profile, profileUri, out);
        installProfile(profileUri);
        return true;
    }

    public boolean storeProfile(URL profile, URI uri, PrintStream out) {
        HttpURLConnection connection = null;
        try {
            String path =  "/domain/contributions/profiles/profile/" + uri;
            connection = domainConnection.put(path, profile);
            int code = connection.getResponseCode();
            if (HttpStatus.UNAUTHORIZED.getCode() == code) {
                out.println("ERROR: Not authorized");
                return false;
            } else if (HttpStatus.FORBIDDEN.getCode() == code && "http".equals(connection.getURL().getProtocol())) {
                out.println("ERROR: An attempt was made to connect using HTTP but the domain requires HTTPS.");
                return false;
            } else if (HttpStatus.CONFLICT.getCode() == code) {
                out.println("ERROR: A profile already exists for " + uri);
                return false;
            } else if (HttpStatus.CREATED.getCode() != code) {
                out.println("ERROR: Error storing profile: " + code);
                return false;
            }
            out.println("Installed " + profileUri);
            return true;
        } catch (FileNotFoundException e) {
            out.println("ERROR: File not found:" + e.getMessage());
            return false;
        }catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain");
            e.printStackTrace(out);
            return false;
        } catch (IOException e) {
            out.println("ERROR: Error installing profile");
            out.println("       " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    public boolean installProfile(URI uri) {
        // NO-OP for now
        return true;
    }

}