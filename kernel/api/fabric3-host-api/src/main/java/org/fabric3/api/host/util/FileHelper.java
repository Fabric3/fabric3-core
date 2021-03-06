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
package org.fabric3.api.host.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.StringTokenizer;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Helper methods for working with files.
 */
public class FileHelper {
    /**
     * The extension separator character.
     */
    private static final char EXTENSION_SEPARATOR = '.';

    /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * Parent directory path
     */
    public static String PARENT_DIRECTORY_INDICATOR = "..";

    /**
     * Current directory path
     */
    public static String CURRENT_DIRECTORY_INDICATOR = ".";

    protected FileHelper() {
    }

    /**
     * Returns the index of the last directory separator character.  This method will handle a file in either Unix or Windows format. The position of the last
     * forward or backslash is returned.  The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there is no such character
     */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Returns the index of the last extension separator character, which is a dot.  This method also checks that there is no directory separator after the last
     * dot. To do this it uses {@link #indexOfLastSeparator(String)} which will handle a file in either Unix or Windows format.  The output will be the same
     * irrespective of the machine that the code is running on.
     *
     * @param filename the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there is no such character
     */
    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? -1 : extensionPos;
    }

    /**
     * Gets the name minus the path from a full filename.  This method will handle a file in either Unix or Windows format. The text after the last forward or
     * backslash is returned.
     * <pre>
     * a/b/c.txt --&gt; c.txt
     * a.txt     --&gt; a.txt
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; &quot;&quot;
     * </pre>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param fileName the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getName(String fileName) {
        if (fileName == null) {
            return null;
        }
        int index = indexOfLastSeparator(fileName);
        return fileName.substring(index + 1);
    }

    /**
     * Gets the extension of a filename.  This method returns the textual part of the filename after the last dot. There must be no directory separator after
     * the dot.
     * <pre>
     * foo.txt      --&gt; &quot;txt&quot;
     * a/b/c.jpg    --&gt; &quot;jpg&quot;
     * a/b.txt/c    --&gt; &quot;&quot;
     * a/b/c        --&gt; &quot;&quot;
     * </pre>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to retrieve the extension of.
     * @return the extension of the file or an empty string if none exists.
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    /**
     * Make a directory, including any necessary but nonexistent parent directories. If there already exists a file with specified name or the directory cannot
     * be created then an exception is thrown.
     *
     * @param directory directory to create, not null
     * @throws NullPointerException if the directory is null
     * @throws Fabric3Exception     if the directory cannot be created
     */
    public static void forceMkdir(File directory) throws Fabric3Exception {
        if (directory == null) {
            return;
        }
        if (directory.exists()) {
            if (directory.isFile()) {
                String message = "File " + directory + " exists and is " + "not a directory. Unable to create directory.";
                throw new Fabric3Exception(message);
            }
        } else {
            if (!directory.mkdirs()) {
                String message = "Unable to create directory " + directory;
                throw new Fabric3Exception(message);
            }
        }
    }

    /**
     * Delete a file. If file is a directory, delete it and all sub-directories.
     *
     * @param file file or directory to delete, not null
     * @throws Fabric3Exception in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws Fabric3Exception {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (!file.exists()) {
                throw new Fabric3Exception("File does not exist: " + file);
            }
            if (!file.delete()) {
                String message = "Unable to delete file: " + file;
                throw new Fabric3Exception(message);
            }
        }
    }

    /**
     * Convert from a <code>URL</code> to a <code>File</code>.  From version 1.1 this method will decode the URL. Syntax such as
     * <code>file:///my%20docs/file.txt</code> will be correctly decoded to <code>/my docs/file.txt</code>.
     *
     * @param url the file URL to convert, null returns null
     * @return the equivalent <code>File</code> object, or <code>null</code> if the URL's protocol is not <code>file</code>
     * @throws IllegalArgumentException if the file is incorrectly encoded
     */
    public static File toFile(URL url) {
        if (url == null || !url.getProtocol().equals("file")) {
            return null;
        } else {
            return new File(toFileString(url));
        }
    }

    /**
     * Convert from a <code>URL</code> to a <code>File</code>.  From version 1.1 this method will decode the URL. Syntax such as
     * <code>file:///my%20docs/file.txt</code> will be correctly decoded to <code>/my docs/file.txt</code>.
     *
     * @param url the file URL to convert, null returns null
     * @return the equivalent <code>File</code> object, or <code>null</code> if the URL's protocol is not <code>file</code>
     * @throws IllegalArgumentException if the file is incorrectly encoded
     */
    public static String toFileString(URL url) {
        if (url == null || !url.getProtocol().equals("file")) {
            return null;
        } else {
            String filename = url.getFile().replace('/', File.separatorChar);
            int pos = 0;
            while ((pos = filename.indexOf('%', pos)) >= 0) { // NOPMD
                if (pos + 2 < filename.length()) {
                    String hexStr = filename.substring(pos + 1, pos + 3);
                    char ch = (char) Integer.parseInt(hexStr, 16);
                    filename = filename.substring(0, pos) + ch + filename.substring(pos + 3);
                }
            }
            return filename;
        }
    }

    /**
     * Clean a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws Fabric3Exception in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws Fabric3Exception {
        if (directory == null) {
            return;
        }
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            throw new Fabric3Exception("Failed to list contents of " + directory);
        }

        Fabric3Exception exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (Fabric3Exception ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Clean a directory without deleting it.
     *
     * @param directory directory to clean, must not be <code>null</code>
     * @throws Fabric3Exception in case cleaning is unsuccessful
     */
    private static void cleanDirectoryOnExit(File directory) throws Fabric3Exception {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            throw new Fabric3Exception("Failed to list contents of " + directory);
        }

        Fabric3Exception exception = null;
        for (File file : files) {
            try {
                forceDeleteOnExit(file);
            } catch (Fabric3Exception ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Copies a whole directory to a new location preserving the file dates.  This method copies the specified directory and all its child directories and files
     * to the specified destination. The destination is the new location and name of the directory.  The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges the source with the destination, with the source taking precedence.
     *
     * @param srcDir  an existing directory to copy, must not be <code>null</code>
     * @param destDir the new directory, must not be <code>null</code>
     * @throws Fabric3Exception if an IO error occurs during copying
     * @since Commons IO 1.1
     */
    public static void copyDirectory(File srcDir, File destDir) throws Fabric3Exception {
        copyDirectory(srcDir, destDir, true);
    }

    /**
     * Copies a whole directory to a new location.  This method copies the contents of the specified source directory to within the specified destination
     * directory.  The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with
     * the destination, with the source taking precedence.
     *
     * @param srcDir           an existing directory to copy, must not be <code>null</code>
     * @param destDir          the new directory, must not be <code>null</code>
     * @param preserveFileDate true if the file date of the copy should be the same as the original
     * @throws Fabric3Exception if source or destination is invalid
     */
    public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws Fabric3Exception {
        try {
            if (srcDir == null) {
                throw new NullPointerException("Source must not be null");
            }
            if (destDir == null) {
                throw new NullPointerException("Destination must not be null");
            }
            if (!srcDir.exists()) {
                throw new Fabric3Exception("Source '" + srcDir + "' does not exist");
            }
            if (!srcDir.isDirectory()) {
                throw new Fabric3Exception("Source '" + srcDir + "' exists but is not a directory");
            }
            if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
                throw new Fabric3Exception("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
            }
            doCopyDirectory(srcDir, destDir, preserveFileDate);
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }

    /**
     * Copies a directory to within another directory preserving the file dates.  This method copies the source directory and all its contents to a directory of
     * the same name in the specified destination directory.  The destination directory is created if it does not exist. If the destination directory did exist,
     * then this method merges the source with the destination, with the source taking precedence.
     *
     * @param srcDir  an existing directory to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws Fabric3Exception if an IO error occurs during copying
     */
    public static void copyDirectoryToDirectory(File srcDir, File destDir) throws Fabric3Exception {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (!(srcDir.exists() && srcDir.isDirectory())) {
            throw new IllegalArgumentException("Source '" + destDir + "' is not a directory");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!(destDir.exists() && destDir.isDirectory())) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        copyDirectory(srcDir, new File(destDir, srcDir.getName()), true);
    }

    /**
     * Copies a file to a new location preserving the file date.  This method copies the contents of the specified source file to the specified destination
     * file. The directory holding the destination file is created if it does not exist. If the destination file exists, then this method will overwrite it.
     *
     * @param srcFile  an existing file to copy, must not be <code>null</code>
     * @param destFile the new file, must not be <code>null</code>
     * @throws Fabric3Exception if an IO error occurs during copying
     */
    public static void copyFile(File srcFile, File destFile) throws Fabric3Exception {
        copyFile(srcFile, destFile, true);
    }

    /**
     * Copies a file to a new location.  This method copies the contents of the specified source file to the specified destination file. The directory holding
     * the destination file is created if it does not exist. If the destination file exists, then this method will overwrite it.
     *
     * @param srcFile          an existing file to copy, must not be <code>null</code>
     * @param destFile         the new file, must not be <code>null</code>
     * @param preserveFileDate true if the file date of the copy should be the same as the original
     * @throws Fabric3Exception if an IO error occurs during copying
     */
    public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws Fabric3Exception {
        try {
            if (srcFile == null) {
                throw new NullPointerException("Source must not be null");
            }
            if (destFile == null) {
                throw new NullPointerException("Destination must not be null");
            }
            if (!srcFile.exists()) {
                throw new Fabric3Exception("Source '" + srcFile + "' does not exist");
            }
            if (srcFile.isDirectory()) {
                throw new Fabric3Exception("Source '" + srcFile + "' exists but is a directory");
            }
            if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
                throw new Fabric3Exception("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
            }
            if (destFile.getParentFile() != null && !destFile.getParentFile().exists()) {
                if (!destFile.getParentFile().mkdirs()) {
                    throw new Fabric3Exception("Destination '" + destFile + "' directory cannot be created");
                }
            }
            if (destFile.exists() && !destFile.canWrite()) {
                throw new Fabric3Exception("Destination '" + destFile + "' exists but is read-only");
            }
            doCopyFile(srcFile, destFile, preserveFileDate);
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }

    /**
     * Copies a file to a directory preserving the file date.  This method copies the contents of the specified source file to a file of the same name in the
     * specified destination directory. The destination directory is created if it does not exist. If the destination file exists, then this method will
     * overwrite it.
     *
     * @param srcFile an existing file to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws Fabric3Exception if an IO error occurs during copying
     */
    public static void copyFileToDirectory(File srcFile, File destDir) throws Fabric3Exception {
        copyFileToDirectory(srcFile, destDir, true);
    }

    /**
     * Copies a file to a directory optionally preserving the file date.  This method copies the contents of the specified source file to a file of the same
     * name in the specified destination directory. The destination directory is created if it does not exist. If the destination file exists, then this method
     * will overwrite it.
     *
     * @param srcFile          an existing file to copy, must not be <code>null</code>
     * @param destDir          the directory to place the copy in, must not be <code>null</code>
     * @param preserveFileDate true if the file date of the copy should be the same as the original
     * @throws Fabric3Exception if an IO error occurs during copying
     * @since Commons IO 1.3
     */
    public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) throws Fabric3Exception {
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!(destDir.exists() && destDir.isDirectory())) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        copyFile(srcFile, new File(destDir, srcFile.getName()), preserveFileDate);
    }

    /**
     * Recursively delete a directory.
     *
     * @param directory directory to delete
     * @throws Fabric3Exception in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws Fabric3Exception {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);
        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new Fabric3Exception(message);
        }
    }

    /**
     * Writes an InputStream to disk.
     *
     * @param source the source stream
     * @param target the target disk location
     * @throws Fabric3Exception if the write encountered an error
     */
    public static void write(InputStream source, File target) throws Fabric3Exception {

        FileChannel channel = null;
        FileLock lock = null;
        try (RandomAccessFile file = new RandomAccessFile(target, "rw")) {
            channel = file.getChannel();
            lock = channel.lock();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            byte[] bytes = buffer.array();
            int limit;
            while (-1 != (limit = source.read(bytes))) {
                buffer.flip();
                buffer.limit(limit);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                buffer.clear();
            }
            channel.force(true);
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        } finally {
            if (channel != null) {
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e) {
                        // ignore
                    }
                }
                try {
                    channel.close();
                } catch (IOException e) {
                    //ignore
                }
            }

        }

    }

    /**
     * Recursively schedule directory for deletion on JVM exit.
     *
     * @param directory directory to delete, must not be <code>null</code>
     * @throws NullPointerException if the directory is <code>null</code>
     * @throws Fabric3Exception     in case deletion is unsuccessful
     */
    private static void deleteDirectoryOnExit(File directory) throws Fabric3Exception {
        if (!directory.exists()) {
            return;
        }

        cleanDirectoryOnExit(directory);
        directory.deleteOnExit();
    }

    /**
     * Internal copy directory method.
     *
     * @param srcDir           the validated source directory, must not be <code>null</code>
     * @param destDir          the validated destination directory, must not be <code>null</code>
     * @param preserveFileDate whether to preserve the file date
     * @throws Fabric3Exception if an error occurs
     */
    private static void doCopyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws Fabric3Exception {
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new Fabric3Exception("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs()) {
                throw new Fabric3Exception("Destination '" + destDir + "' directory cannot be created");
            }
            if (preserveFileDate) {
                destDir.setLastModified(srcDir.lastModified());
            }
        }
        if (!destDir.canWrite()) {
            throw new Fabric3Exception("Destination '" + destDir + "' cannot be written to");
        }
        // recurse
        File[] files = srcDir.listFiles();
        if (files == null) { // null if security restricted
            throw new Fabric3Exception("Failed to list contents of " + srcDir);
        }
        for (File file : files) {
            File copiedFile = new File(destDir, file.getName());
            if (file.isDirectory()) {
                doCopyDirectory(file, copiedFile, preserveFileDate);
            } else {
                doCopyFile(file, copiedFile, preserveFileDate);
            }
        }
    }

    /**
     * Internal copy file method.
     *
     * @param srcFile          the validated source file, must not be <code>null</code>
     * @param destFile         the validated destination file, must not be <code>null</code>
     * @param preserveFileDate whether to preserve the file date
     * @throws Fabric3Exception if an error occurs
     */
    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws Fabric3Exception {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new Fabric3Exception("Destination '" + destFile + "' exists but is a directory");
        }
        FileOutputStream output = null;
        try (FileInputStream input = new FileInputStream(srcFile)) {
            output = new FileOutputStream(destFile);
            IOHelper.copy(input, output);
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        if (srcFile.length() != destFile.length()) {
            throw new Fabric3Exception("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }

    /**
     * Schedule a file to be deleted when JVM exits. If file is directory delete it and all sub-directories.
     *
     * @param file file or directory to delete, must not be <code>null</code>
     * @throws NullPointerException if the file is <code>null</code>
     * @throws Fabric3Exception     in case deletion is unsuccessful
     */
    public static void forceDeleteOnExit(File file) throws Fabric3Exception {
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * Given a parentLocation as a base, returns the absolute path to the childLocation. The childLocation may be absolute or relative.  If the child is
     * absolute, it is simply returned unchanged.  If it is relative, this method then resolves the location of the child from the parent location.
     */
    public static String resolveRelativePath(String parentLocation, String childLocation) {
        String path = childLocation;
        String fwdSlash = "/";

        // just return the path as-is if either location is null or empty
        if (path == null || "".equals(path) || parentLocation == null || "".equals(parentLocation)) {
            return path;
        }

        // resolve relative path
        if (!isAbsolute(path)) {
            String basePath = parentLocation;
            basePath = basePath.replace('\\', '/');
            basePath = basePath.substring(0, basePath.lastIndexOf('/') + 1);

            if (path.startsWith(CURRENT_DIRECTORY_INDICATOR + fwdSlash)) {
                path = path.substring(2);
            } else if (path.startsWith(PARENT_DIRECTORY_INDICATOR)) {
                // if we have a parent directory indicator, then strip off segments
                // from the basePath until there are no more '..'
                StringBuilder builder = new StringBuilder();
                StringTokenizer tokenizer = new StringTokenizer(path, fwdSlash);
                int countParentInd = 0;
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    if (PARENT_DIRECTORY_INDICATOR.equals(token)) {
                        countParentInd++;
                    } else {
                        builder.append(fwdSlash).append(token);
                    }
                }

                if (basePath.endsWith(fwdSlash)) {
                    basePath = basePath.substring(0, basePath.length() - 1);
                }

                // remove segments from the basePath
                for (int i = 0; i < countParentInd; i++) {

                    int lastIdx = basePath.lastIndexOf(fwdSlash);
                    basePath = basePath.substring(0, lastIdx);

                    // if the basePath is now the root, then add back a parent dir. ind.
                    // and break from this loop.
                    if (i != countParentInd - 1 && ((basePath.endsWith(fwdSlash) && basePath.length() == 1) || basePath.endsWith(":"))) {
                        int addParentIndCount = countParentInd - 1 - i;
                        for (int j = 0; j < addParentIndCount; j++) {
                            if (basePath.endsWith(":") || basePath.endsWith(PARENT_DIRECTORY_INDICATOR)) {
                                basePath += fwdSlash + PARENT_DIRECTORY_INDICATOR;
                            } else {
                                basePath += PARENT_DIRECTORY_INDICATOR;
                            }
                        }
                        break;
                    }
                }

                path = builder.toString();
            }

            path = basePath + path;
        }
        return path;
    }

    /**
     * Determine if the given String represents an absolute path by checking if the string starts with a '/' or is a URI that starts with a scheme 'scheme:/'.
     *
     * @return true if path is absolute, otherwise false.
     */
    public static boolean isAbsolute(String aPath) {
        boolean absolute = false;
        String path = aPath.replace('\\', '/');
        // the path has a scheme if a colon appears before the first '/'
        if (path.indexOf(':') > 0 && path.indexOf('/') > path.indexOf(':')) {
            absolute = true;
        }
        // starts with a '/'
        else if (path.startsWith("/")) {
            absolute = true;
        }

        return absolute;
    }
}
