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
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Map;

import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.admin.interpreter.communication.CommunicationException;
import org.fabric3.admin.interpreter.communication.DomainConnection;

/**
 * @version $Rev$ $Date$
 */
public class GetCommand implements Command {
    private static final String SELF_LINK = "selfLink";
    private static final String LINKS = "links";

    private DomainConnection domainConnection;
    private String path;
    private boolean verbose;
    private String username;
    private String password;

    public GetCommand(DomainConnection domainConnection) {
        this.domainConnection = domainConnection;
    }

    public void setPath(String path) {
        //  accept relative and absolute forms
        if (!path.startsWith("/")) {
            this.path = "/" + path;
        } else {
            this.path = path;
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
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
        HttpURLConnection connection = null;
        try {
            connection = domainConnection.createConnection(path, "GET");
            int code = connection.getResponseCode();
            if (HttpStatus.UNAUTHORIZED.getCode() == code) {
                out.println("ERROR: Not authorized");
                return false;
            } else if (HttpStatus.FORBIDDEN.getCode() == code && "http".equals(connection.getURL().getProtocol())) {
                out.println("An attempt was made to connect using HTTP but the domain requires HTTPS.");
                return false;
            } else if (HttpStatus.NOT_FOUND.getCode() == code) {
                out.println("Resource not found");
                return false;
            } else {
                InputStream stream = connection.getInputStream();
                Object value = domainConnection.parse(Object.class, stream);
                printOutput("", value, code, 0, out);
                return true;
            }
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

    private void printOutput(Object key, Object value, int code, int indent, PrintStream out) {
        String space = "";
        for (int i = 0; i <= indent; i++) {
            space = space + " ";
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.isEmpty()) {
                if (indent == 0) {
                    out.println(space + "Response code: " + code);
                }
            } else {
                if (SELF_LINK.equals(key)) {
                    return;
                }
                if (indent > 0) {
                    out.println(space + key + ":");
                }
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (SELF_LINK.equals(entry.getKey())) {
                        continue;
                    }
                    Object contained = entry.getValue();
                    if (contained instanceof Map) {
                        Map<?, ?> subMap = (Map<?, ?>) contained;
                        if (isLink(subMap)) {
                            if (!verbose) {
                                // skip links
                                continue;
                            }
                            out.println(space + entry.getKey() + " : " + subMap.get("href"));
                        } else {
                            printOutput(entry.getKey(), contained, code, indent + 3, out);
                        }
                    } else if (contained instanceof Collection && !verbose && LINKS.equals(entry.getKey())) {
                        // skip links
                        continue;
                    } else {
                        out.println(space + entry.getKey() + " : " + contained);
                    }
                }
            }

        } else {
            if (!SELF_LINK.equals(key)) {
                out.println(space + key + " : " + value);
            }
        }
    }

    private boolean isLink(Map<?, ?> map) {
        return map.containsKey("name") && map.containsKey("rel") && map.containsKey("href");
    }

}