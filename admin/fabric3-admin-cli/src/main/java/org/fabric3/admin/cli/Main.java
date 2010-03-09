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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;


/**
 * Launcher for the domain administation command line tool.
 *
 * @version $Rev$ $Date$
 */
public class Main {
    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:1199/server";

    /**
     * Executes either a single instruction passed from the command line or enters into interactive mode.
     * <p/>
     * The interpreter is loaded in a child classloader so the contents of the /lib directory can be dynamically placed on the classpath. This allows
     * the interpreter to be extended with alternative JMX protocol providers than those present in the JDK.
     *
     * @param args the instruction to execture or an empty array
     * @throws Exception if there is an error executing the instruction or entering interactive mode
     */
    public static void main(String[] args) throws Exception {
        ClassLoader loader = createClassLoader(Main.class.getClassLoader());

        // set the context classloader as some JMX provders require it
        Thread.currentThread().setContextClassLoader(loader);

        Class<?> controllerInterface = loader.loadClass("org.fabric3.admin.api.DomainController");
        Class<?> settingsInterface = loader.loadClass("org.fabric3.admin.interpreter.Settings");
        Class<?> settingsClass = loader.loadClass("org.fabric3.admin.interpreter.impl.FileSettings");
        Class<?> controllerImplClass = loader.loadClass("org.fabric3.admin.impl.DomainControllerImpl");
        Class<?> interpreterClass = loader.loadClass("org.fabric3.admin.interpreter.impl.InterpreterImpl");
        Class<?> domainConfigClass = loader.loadClass("org.fabric3.admin.interpreter.DomainConfiguration");

        // load the Settings
        Object settings = settingsClass.getConstructor(File.class).newInstance(getSettingsFile());
        try {
            settingsClass.getMethod("load").invoke(settings);
        } catch (InvocationTargetException e) {
            System.out.println("Error loading settings file. Exception was:");
            e.getCause().printStackTrace();
            return;
        }
        Method getMethod = settingsClass.getMethod("getDomainConfiguration", String.class);
        Object domainConfiguration = getMethod.invoke(settings, "default");
        if (domainConfiguration == null) {
            // create a default configuration if one does not exist
            Method addMethod = settingsClass.getMethod("addConfiguration", domainConfigClass);
            Constructor<?> ctor = domainConfigClass.getConstructor(String.class, String.class, String.class, String.class, String.class);
            Object configuration = ctor.newInstance("default", JMX_URL, null, null, null);
            addMethod.invoke(settings, configuration);
        }

        // load the DomainController
        Object controller = controllerImplClass.newInstance();

        // load the Interpreter
        Constructor ctor = interpreterClass.getConstructor(controllerInterface, settingsInterface);
        Object interpreter = ctor.newInstance(controller, settings);
        if (args.length == 0) {
            // interactive mode
            System.out.println("\nFabric3 Admininstration Interface");
            Method method = interpreterClass.getMethod("processInteractive", InputStream.class, PrintStream.class);
            method.invoke(interpreter, System.in, System.out);
        } else {
            // execute the command passed in
            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(" ").append(arg);
            }
            Method method = interpreterClass.getMethod("process", String.class, PrintStream.class);
            method.invoke(interpreter, builder.toString(), System.out);
        }
    }

    /**
     * Returns the location of the settings.xml file by introspecting the location of the current class. It is assumed the settings file is
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
        return new File(configDir, "settings.xml");
    }

    /**
     * Create a classloader from all the jar files lib directory. The classpath for the returned classloader will comprise all jar files and
     * subdirectories of the supplied directory. Hidden files and those that do not contain a valid manifest will be silently ignored.
     *
     * @param parent the parent for the new classloader
     * @return a classloader whose classpath includes all jar files and subdirectories of the supplied directory
     */
    public static ClassLoader createClassLoader(ClassLoader parent) {
        File directory = getLibDirectory();
        File[] jars = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isHidden()) {
                    return false;
                }
                if (file.isDirectory()) {
                    return true;
                }
                try {
                    JarFile jar = new JarFile(file);
                    return jar.getManifest() != null;
                } catch (IOException e) {
                    return false;
                }
            }
        });

        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                // toURI should have escaped the URL
                throw new AssertionError();
            }
        }

        return new URLClassLoader(urls, parent);
    }

    public static File getLibDirectory() throws IllegalStateException {

        // get the name of the Class's bytecode
        String name = Main.class.getName();
        int last = name.lastIndexOf('.');
        if (last != -1) {
            name = name.substring(last + 1);
        }
        name = name + ".class";

        // get location of the bytecode - should be a jar: URL
        URL url = Main.class.getResource(name);
        if (url == null) {
            throw new IllegalStateException("Unable to get location of bytecode resource " + name);
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
        return new File(jarFile.getParentFile().getParentFile(), "lib");
    }


}
