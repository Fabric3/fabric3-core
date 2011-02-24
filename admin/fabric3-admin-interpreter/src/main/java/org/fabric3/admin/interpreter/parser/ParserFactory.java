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
package org.fabric3.admin.interpreter.parser;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.admin.interpreter.CommandParser;
import org.fabric3.admin.interpreter.Interpreter;
import org.fabric3.admin.interpreter.Settings;
import org.fabric3.admin.interpreter.communication.DomainConnection;

/**
 * @version $Rev$ $Date$
 */
public class ParserFactory {

    private ParserFactory() {
    }

    /**
     * Initializes the command parsers
     */
    public static Map<String, CommandParser> createParsers(DomainConnection domainConnection, Interpreter interpreter, Settings settings) {
        Map<String, CommandParser> parsers = new HashMap<String, CommandParser>();
        AuthCommandParser authenticateParser = new AuthCommandParser(domainConnection);
        parsers.put("au", authenticateParser);
        parsers.put("authenticate", authenticateParser);
        InstallCommandParser installParser = new InstallCommandParser(domainConnection);
        parsers.put("install", installParser);
        parsers.put("ins", installParser);
        StatCommandParser statusParser = new StatCommandParser(domainConnection);
        parsers.put("status", statusParser);
        parsers.put("st", statusParser);
        DeployCommandParser deployParser = new DeployCommandParser(domainConnection);
        parsers.put("deploy", deployParser);
        parsers.put("de", deployParser);
        UndeployCommandParser undeployParser = new UndeployCommandParser(domainConnection);
        parsers.put("undeploy", undeployParser);
        parsers.put("ude", undeployParser);
        UninstallCommandParser uninstallParser = new UninstallCommandParser(domainConnection);
        parsers.put("uninstall", uninstallParser);
        parsers.put("uin", uninstallParser);
        parsers.put("use", new UseCommandParser(domainConnection, settings));
        ProvisionCommandParser provisionParser = new ProvisionCommandParser(domainConnection);
        parsers.put("pr", provisionParser);
        parsers.put("provision", provisionParser);
        ListCommandParser listCommandParser = new ListCommandParser(domainConnection);
        parsers.put("ls", listCommandParser);
        parsers.put("list", listCommandParser);
        ProfileCommandParser profileCommandParser = new ProfileCommandParser(domainConnection);
        parsers.put("profile", profileCommandParser);
        parsers.put("pf", profileCommandParser);
        RunCommandParser runCommandParser = new RunCommandParser(interpreter);
        parsers.put("run", runCommandParser);
        parsers.put("r", runCommandParser);

        return parsers;
    }

}
