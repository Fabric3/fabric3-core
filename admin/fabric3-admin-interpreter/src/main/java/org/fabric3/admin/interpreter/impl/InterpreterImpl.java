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
package org.fabric3.admin.interpreter.impl;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.parser.ParserFactory;
import org.fabric3.admin.interpreter.Interpreter;
import org.fabric3.admin.interpreter.Settings;
import org.fabric3.admin.interpreter.CommandParser;
import org.fabric3.admin.interpreter.TransientSettings;
import org.fabric3.admin.interpreter.InterpreterException;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;

/**
 * Default interpreter implementation. This implementation constructs a parse tree from an instruction as defined by the domain adminsitration
 * grammar. This tree is then transformed into an AST which is traversed to produce a set of commands to execute against the DomainController.
 * <p/>
 * Antlr3 is used as the parser technology to construct the parse tree and AST.
 *
 * @version $Rev$ $Date$
 */
public class InterpreterImpl implements Interpreter {
    private static final String PROMPT = "\nf3>";
    private static final String HELP = "help";
    private static final String HELP_TEXT = "Type help <subcommand> for more information: \n\n"
            + "   authenticate (au) \n"
            + "   deploy (de) \n"
            + "   install (ins) \n"
            + "   list (ls) \n"
            + "   profile (pf) \n"
            + "   provision (pr) \n"
            + "   status (st) \n"
            + "   undeploy (ude) \n"
            + "   uninstall (uin) \n"
            + "   use \n";

    private DomainController controller;
    private Settings settings;
    private Map<String, CommandParser> parsers;

    public InterpreterImpl(DomainController controller) {
        this(controller, new TransientSettings());
    }

    public InterpreterImpl(DomainController controller, Settings settings) {
        this.controller = controller;
        this.settings = settings;
        parsers = ParserFactory.createParsers(controller, settings);
        setDefaultAddress();
    }

    public void processInteractive(InputStream in, PrintStream out) {
        Scanner scanner = new Scanner(in);
        while (true) {
            out.print(PROMPT);
            String line = scanner.nextLine().trim();
            if ("quit".equals(line) || "exit".equals(line)) break;
            try {
                process(line, out);
            } catch (InterpreterException e) {
                // TODO handle this better
                e.printStackTrace();
            }
        }
    }


    public void process(String line, PrintStream out) throws InterpreterException {
        // parse the command, strip whitespace and tokenize the command line
        line = line.trim();
        String commandString;
        String tokens[];
        int index = line.indexOf(" ");
        if (index == -1) {
            commandString = line;
            tokens = new String[0];
        } else {
            commandString = line.substring(0, index);
            String replaced = line.substring(index + 1).replaceAll("\\s{2,}", " ");
            tokens = replaced.split(" ");
        }
        if (HELP.equals(commandString)) {
            if (tokens.length == 0) {
                out.println(HELP_TEXT);
            } else {
                CommandParser parser = parsers.get(tokens[0]);
                if (parser == null) {
                    throw new InterpreterException("Unrecognized command: " + commandString);
                }
                out.println(parser.getUsage());
            }

            return;
        }
        CommandParser parser = parsers.get(commandString);
        if (parser == null) {
            throw new InterpreterException("Unrecognized command: " + commandString);
        }
        Command command = parser.parse(tokens);
        try {
            command.execute(out);
        } catch (CommandException e) {
            out.println("ERORR: An error was encountered");
            e.printStackTrace(out);
        }
    }

    /**
     * Sets the default domain address if it is configured.
     */
    private void setDefaultAddress() {
        String defaultAddress = settings.getDomainAddress("default");
        if (defaultAddress != null) {
            controller.setDomainAddress(defaultAddress);
        }
    }

}
