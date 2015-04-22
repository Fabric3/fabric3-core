/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.runtime.standalone.server;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Shuts down a server instance by placing a {@code f3.shutdown} file in the runtime data directory.
 *
 * The main method takes one parameter, the runtime name, and defaults to "vm".
 */
public class Shutdown {

    public static void main(String[] args) {
        String runtimeName = args.length > 0 ? args[0] : "vm";
        File installDir = getInstallDirectory(Shutdown.class);
        File dataDir = new File(installDir, "runtimes" + File.separatorChar + runtimeName + File.separatorChar + "data");
        try {
            if (!dataDir.exists()) {
                System.out.println("Runtime configuration does not exist. Unable to shutdown: " + runtimeName);
                return;
            }
            new File(dataDir, "f3.shutdown").createNewFile();
            System.out.println("Fabric3 shutting down");
        } catch (IOException e) {
            System.out.println("Unable to shutdown server: " + runtimeName);
            e.printStackTrace();
        }

    }

    /**
     * Gets the installation directory based on the location of a class file. The installation directory is calculated by determining the path of the jar
     * containing the given class file and returning its parent directory.
     *
     * @param clazz the class to use as a way to find the executable jar
     * @return directory where Fabric3 runtime is installed.
     * @throws IllegalStateException if the location could not be determined from the location of the class file
     */
    public static File getInstallDirectory(Class<?> clazz) throws IllegalStateException {
        // get the name of the Class's bytecode
        String name = clazz.getName();
        int last = name.lastIndexOf('.');
        if (last != -1) {
            name = name.substring(last + 1);
        }
        name = name + ".class";

        // get location of the bytecode - should be a jar: URL
        URL url = clazz.getResource(name);
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
        return jarFile.getParentFile().getParentFile();
    }

}
