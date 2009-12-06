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
package org.fabric3.admin.cli;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.impl.DomainControllerImpl;
import org.fabric3.admin.interpreter.Interpreter;
import org.fabric3.admin.interpreter.InterpreterException;
import org.fabric3.admin.interpreter.impl.InterpreterImpl;

/**
 * Main entry point for the domain administation command line tool.
 *
 * @version $Rev$ $Date$
 */
public class Main {

    /**
     * Executes either a single instruction passed from the command line or enters into interactive mode.
     *
     * @param args the instruction to execture or an empty array
     * @throws InterpreterException if an error occurs executing an instruction or set of instructions
     */
    public static void main(String[] args) throws InterpreterException {
        DomainController controller = new DomainControllerImpl();
        FileSettings settings = new FileSettings(getSettingsFile());
        try {
            settings.load();
            if (settings.getDomainAddress("default") == null) {
                settings.addDomain("default", "service:jmx:rmi:///jndi/rmi://localhost:1199/server");
            }
        } catch (IOException e) {
            throw new InterpreterException("Error loading settings", e);
        }
        Interpreter interpreter = new InterpreterImpl(controller, settings);
        if (args.length == 0) {
            System.out.println("\nFabric3 Admininstration Interface");
            interpreter.processInteractive(System.in, System.out);
        } else {
            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(" ").append(arg);
            }
            interpreter.process(builder.toString(), System.out);
        }
    }

    /**
     * Returns the location of the settings.properties file by introspecting the location of the current class. It is assumed the settings file is
     * contained in a sibling directory named "config".
     *
     * @return the location of the settings file
     * @throws IllegalStateException if the class cannot be introspected
     */
    private static File getSettingsFile() throws IllegalStateException {
        Class<?> clazz = Main.class;
        String name = clazz.getName();
        int last = name.lastIndexOf('.');
        if (last != -1) {
            name = name.substring(last + 1);
        }
        name = name + ".class";

        // get location of the class
        URL url = clazz.getResource(name);
        if (url == null) {
            throw new IllegalStateException("Unable to get location of class " + name);
        }

        String jarLocation = url.toString();
        if (!jarLocation.startsWith("jar:")) {
            throw new IllegalStateException("Must be run from a jar: " + url);
        }

        // extract the location of thr jar from the resource URL
        jarLocation = jarLocation.substring(4, jarLocation.lastIndexOf("!/"));
        if (!jarLocation.startsWith("file:")) {
            throw new IllegalStateException("Must be run from a local filesystem: " + jarLocation);
        }

        File jarFile = new File(URI.create(jarLocation));
        File configDir = new File(jarFile.getParentFile().getParentFile(), "config");
        return new File(configDir, "settings.properties");
    }


}
