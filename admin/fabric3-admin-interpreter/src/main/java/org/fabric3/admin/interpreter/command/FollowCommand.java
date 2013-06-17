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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.admin.interpreter.communication.CommunicationException;
import org.fabric3.admin.interpreter.communication.DomainConnection;

/**
 *
 */
public class FollowCommand implements Command {
    private static final String ZONE_RESOURCE = "/domain/zones";
    private static final String RUNTIME_RESOURCE = "/domain/runtimes";
    private DomainConnection domainConnection;
    private String zone;
    private String runtime;

    public FollowCommand(DomainConnection domainConnection) {
        this.domainConnection = domainConnection;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public void setUsername(String username) {
        // no-op
    }

    public void setPassword(String password) {
        // no-op
    }

    public boolean execute(PrintStream out) throws CommandException {
        if (zone != null) {
            follow(zone, ZONE_RESOURCE, out);
        } else {
            follow(runtime, RUNTIME_RESOURCE, out);
        }
        return true;
    }

    private boolean follow(String name, String resourceAddress, PrintStream out) throws CommandException {
        HttpURLConnection connection = null;
        try {
            connection = domainConnection.createControllerConnection(resourceAddress, "GET");
            if (connect(out, connection)) {
                return false;
            }
            InputStream stream = connection.getInputStream();
            String address = parseAddress(name, stream);
            if (address == null) {
                out.println("Not found or online");
                return false;
            }
            String managementAddress = address.substring(0, address.lastIndexOf("/"));
            domainConnection.pushAddress(name, managementAddress);
            out.println("Set management to " + name);
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to " + name);
            e.printStackTrace(out);
            return false;
        } catch (IOException e) {
            out.println("ERROR: Error connecting to " + name + " " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return true;
    }

    private boolean connect(PrintStream out, HttpURLConnection connection) throws IOException {
        connection.connect();
        int code = connection.getResponseCode();
        if (HttpStatus.UNAUTHORIZED.getCode() == code) {
            out.println("ERROR: Not authorized");
            return true;
        } else if (HttpStatus.FORBIDDEN.getCode() == code && "http".equals(connection.getURL().getProtocol())) {
            out.println("ERROR: Attempt made to connect using HTTP but the domain requires HTTPS.");
            return true;
        } else if (HttpStatus.NOT_FOUND.getCode() == code) {
            out.println("Zone not found");
            return true;
        } else if (HttpStatus.OK.getCode() != code) {
            out.println("ERROR: Server error: " + code);
            return true;
        }
        return false;
    }

    private String parseAddress(String name, InputStream stream) throws IOException {
        Map<String, List<Map<String, String>>> value = domainConnection.parse(Object.class, stream);
        // Resolve the zone or runtime HTTP address which is stored in a collection of Map-based links
        // The address is in the form: href=http://<ip>/management/zone or href=http://<ip>/management/runtime
        List<Map<String, String>> list = value.get("value");
        String address = null;
        for (Map<String, String> entry : list) {
            if (name.equals(entry.get("name"))) {
                address = entry.get("href");
                break;
            }
        }
        return address;
    }

}