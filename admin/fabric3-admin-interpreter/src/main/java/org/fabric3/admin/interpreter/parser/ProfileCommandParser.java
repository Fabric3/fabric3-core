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
package org.fabric3.admin.interpreter.parser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandParser;
import org.fabric3.admin.interpreter.ParseException;
import org.fabric3.admin.interpreter.command.InstallProfileCommand;
import org.fabric3.admin.interpreter.command.UninstallProfileCommand;

/**
 * @version $Rev$ $Date$
 */
public class ProfileCommandParser implements CommandParser {
    private DomainController controller;

    public ProfileCommandParser(DomainController controller) {
        this.controller = controller;
    }

    public String getUsage() {
        return "profile (pf): Installs a profile to the domain repository.\n" +
                "usage: profile install <profile> [-u username -p password]";
    }

    public Command parse(String[] tokens) throws ParseException {
        if (tokens.length != 2 && tokens.length != 6) {
            throw new ParseException("Illegal number of arguments");
        }
        if ("install".equals(tokens[0])) {
            return install(tokens);
        } else if ("uninstall".equals(tokens[0])) {
            return uninstall(tokens);
        } else {
            throw new ParseException("Unknown profile command: " + tokens[1]);
        }
    }

    private Command uninstall(String[] tokens) throws ParseException {
        UninstallProfileCommand command = new UninstallProfileCommand(controller);
        try {
            command.setProfileUri(new URI(tokens[1]));
        } catch (URISyntaxException e) {
            throw new ParseException("Invalid profile name", e);
        }
        if (tokens.length == 6) {
            ParserHelper.parseAuthorization(command, tokens, 2);
        }
        return command;
    }

    private Command install(String[] tokens) throws ParseException {
        InstallProfileCommand command = new InstallProfileCommand(controller);
        try {
            URL url = ParserHelper.parseUrl(tokens[1]);
            command.setProfile(url);
        } catch (MalformedURLException e) {
            throw new ParseException("Invalid profile URL", e);
        }
        if (tokens.length == 6) {
            ParserHelper.parseAuthorization(command, tokens, 2);
        }
        return command;
    }

}