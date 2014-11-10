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
 */
package org.fabric3.node;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.fabric3.api.node.FabricException;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.util.IOHelper;

/**
 * Methods for performing operations on JAR files.
 */
public class ArchiveUtils {

    /**
     * Returns the directory where the JAR containing the given class is located on the file system.
     *
     * @param clazz the class
     * @return the directory where the JAR containing the given class is located
     * @throws IllegalStateException if there is an error returning the directory
     */
    public static File getJarDirectory(Class<?> clazz) throws IllegalStateException {
        // get the name of the Class's bytecode
        String name = getClassFileName(clazz);

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
        return jarFile.getParentFile();
    }

    /**
     * Returns the archive file for the given profile in the Maven-based repository directory.
     * <p/>
     * The search algorithm is simple: calculate the Maven archive name using profile-[name]-[version]-bin.zip and find it relative to the provided directory
     *
     * @param profile   the profile name; if not prefixed with 'profile-', it will be appended/
     * @param directory the repository directory
     * @return the archive file
     */
    public static File getProfileArchive(String profile, File directory) {
        String name = profile;
        if (!name.startsWith("profile-")) {
            // add profile- prefix if not present
            name = "profile-" + name;
        }
        File profileDirectory = new File(directory, name);
        if (!profileDirectory.exists()) {
            throw new FabricException("Profile not found in repository: " + profile);
        }
        File profileArchiveDirectory = new File(profileDirectory, Names.VERSION);
        if (!profileArchiveDirectory.exists()) {
            profileArchiveDirectory = new File(profileDirectory, Names.VERSION + "-SNAPSHOT");
        }
        if (!profileArchiveDirectory.exists()) {
            throw new FabricException("Profile version not found in repository: " + profile);
        }
        File profileArchive = new File(profileArchiveDirectory, name + "-" + Names.VERSION + "-bin.zip");
        if (!profileArchive.exists()) {
            profileArchive = new File(profileArchiveDirectory, name + "-" + Names.VERSION + "-SNAPSHOT-bin.zip");
        }
        if (!profileArchive.exists()) {
            throw new FabricException("Profile archive not found in repository: " + profile);
        }
        return profileArchive;
    }

    /**
     * Returns the archive file for the given extension in the Maven-based repository directory.
     * <p/>
     * The search algorithm is simple: calculate the Maven archive name using [name]-[version].jar and find it relative to the provided directory
     *
     * @param extension the extension name, which is the Maven artifact id
     * @param directory the repository directory
     * @return the archive file
     */
    public static File getExtensionArchive(String extension, File directory) {
        File profileDirectory = new File(directory, extension);
        if (!profileDirectory.exists()) {
            throw new FabricException("Extension archive not found in repository: " + extension);
        }
        File profileArchiveDirectory = new File(profileDirectory, Names.VERSION);
        if (!profileArchiveDirectory.exists()) {
            profileArchiveDirectory = new File(profileDirectory, Names.VERSION + "-SNAPSHOT");
        }
        if (!profileArchiveDirectory.exists()) {
            throw new FabricException("Extension archive version not found in repository: " + extension);
        }
        File profileArchive = new File(profileArchiveDirectory, extension + "-" + Names.VERSION + ".jar");
        if (!profileArchive.exists()) {
            profileArchive = new File(profileArchiveDirectory, extension + "-" + Names.VERSION + "-SNAPSHOT.jar");
        }
        if (!profileArchive.exists()) {
            throw new FabricException("Extension archive not found in repository: " + extension);
        }
        return profileArchive;
    }

    /**
     * Expands the JAR and ZIP contents of an archive into the given destination directory.
     *
     * @param archive     the archive
     * @param destination the destination directory
     * @throws IOException if there is an error expanding the archive
     */
    public static List<File> unpack(File archive, File destination) throws IOException {
        List<File> expandedFiles = new ArrayList<>();
        JarInputStream jarStream = null;
        try {

            jarStream = new JarInputStream(new FileInputStream(archive));
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String simpleName = parseSimpleName(entry);
                if (simpleName == null) {
                    continue;
                }
                File jarFile = new File(destination, simpleName);
                if (jarFile.exists()) {
                    // jar already exists, skip
                    continue;
                }
                copy(jarStream, jarFile);
                expandedFiles.add(jarFile);
            }
            return expandedFiles;
        } finally {
            IOHelper.closeQuietly(jarStream);
        }
    }

    /**
     * Returns the name of a JAR or ZIP entry without the path.
     *
     * @param entry the JAR entry
     * @return the JAR entry without the path
     */
    private static String parseSimpleName(JarEntry entry) {
        int index = entry.getName().lastIndexOf("/");
        String simpleName;
        if (index > 0) {
            simpleName = entry.getName().substring(index);
        } else {
            simpleName = entry.getName();
        }

        if (!simpleName.endsWith(".jar") && !simpleName.endsWith(".zip")) {   // support jars and profile zips
            return null;

        }
        return simpleName;
    }

    /**
     * Copies a stream to a destination
     *
     * @param stream      the stream
     * @param destination the destination
     * @throws IOException if there is an error copying
     */
    private static void copy(JarInputStream stream, File destination) throws IOException {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(destination))) {
            IOHelper.copy(stream, os);
            os.flush();
        }
        destination.deleteOnExit();
    }

    /**
     * Returns the class file name including the full path.
     *
     * @param clazz the class
     * @return the class file name
     */
    private static String getClassFileName(Class<?> clazz) {
        String name = clazz.getName();
        int last = name.lastIndexOf('.');
        if (last != -1) {
            name = name.substring(last + 1);
        }
        name = name + ".class";
        return name;
    }

    private ArchiveUtils() {
    }
}
