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
package org.fabric3.admin.interpreter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;

import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.admin.interpreter.CommandParser;
import org.fabric3.admin.interpreter.DomainConfiguration;
import org.fabric3.admin.interpreter.Interpreter;
import org.fabric3.admin.interpreter.ParseException;
import org.fabric3.admin.interpreter.Settings;
import org.fabric3.admin.interpreter.TransientSettings;
import org.fabric3.admin.interpreter.communication.DomainConnection;
import org.fabric3.admin.interpreter.parser.ParserFactory;

/**
 * Default interpreter implementation.
 */
public class InterpreterImpl implements Interpreter {
    private static final String PROMPT = "\nf3>";
    private static final String HELP = "help";
    private static final String HELP_TEXT = "Type help <command> for more information: \n\n"
            + "   authenticate (au) \n"
            + "   back (b) \n"
            + "   deploy (de) \n"
            + "   follow (f) \n"
            + "   get (g) \n"
            + "   install (ins) \n"
            + "   list (ls) \n"
            + "   post (p) \n"
            + "   profile (pf) \n"
            + "   provision (pr) \n"
            + "   quit (q) \n"
            + "   status (st) \n"
            + "   undeploy (ude) \n"
            + "   uninstall (uin) \n"
            + "   use \n"
            + "   run (r) \n";

    private DomainConnection domainConnection;
    private Settings settings;
    private Map<String, CommandParser> parsers;

    public InterpreterImpl(DomainConnection domainConnection) {
        this(domainConnection, new TransientSettings());
    }

    public InterpreterImpl(DomainConnection domainConnection, Settings settings) {
        this.domainConnection = domainConnection;
        this.settings = settings;
        parsers = ParserFactory.createParsers(domainConnection, this, settings);
        setDefaultConfiguration();
    }

    public void processInteractive(InputStream in, PrintStream out) {
        DomainConfiguration configuration = settings.getDomainConfiguration("default");
        if (configuration != null) {
            out.println("Using " + configuration.getName() + " [" + configuration.getAddress() + "]");
        }
        try {
            ConsoleReader reader = createReader(in, out);
            String line;
            while ((line = reader.readLine(PROMPT)) != null) {
                if ("quit".equals(line) || "q".equals(line) || "exit".equals(line)) {
                    break;
                } else if (line.trim().length() == 0) {
                    continue;
                }
                process(line, out);
            }
        } catch (IOException e) {
            out.println("Error launching interpreter");
            e.printStackTrace();
        }

    }

    public void process(String line, PrintStream out) {
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
                    out.println("Unrecognized command: " + commandString);
                } else {
                    out.println(parser.getUsage());
                }
            }

            return;
        }
        CommandParser parser = parsers.get(commandString);
        if (parser == null) {
            out.println("Unrecognized command: " + commandString);
            return;
        }
        try {
            Command command = parser.parse(tokens);
            command.execute(out);
        } catch (ParseException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (CommandException e) {
            out.println("ERROR: An error was encountered");
            e.printStackTrace(out);
        }
    }

    /**
     * Sets the default domain address if it is configured.
     */
    private void setDefaultConfiguration() {
        DomainConfiguration configuration = settings.getDomainConfiguration("default");
        if (configuration != null) {
            domainConnection.setAddress(configuration.getName(), configuration.getAddress());
            domainConnection.setUsername(configuration.getUsername());
            domainConnection.setPassword(configuration.getPassword());
        }
    }

    private ConsoleReader createReader(InputStream in, PrintStream out) throws IOException {
        ConsoleReader reader = new ConsoleReader(in, new OutputStreamWriter(out));
        List<Completer> completors = new ArrayList<>();
        String[] commands =
                {"authenticate", "back", "deploy", "follow", "get", "install", "list", "post", "profile", "provision", "status", "undeploy", "uninstall", "use", "run", "quit"};
        StringsCompleter simpleCompletor = new StringsCompleter(commands);
        completors.add(simpleCompletor);
        FileNameCompleter fileCompletor = new FileNameCompleter();
        completors.add(fileCompletor);
        ArgumentCompleter argumentCompletor = new ArgumentCompleter(completors);
        reader.addCompleter(argumentCompletor);
        return reader;
    }


}
