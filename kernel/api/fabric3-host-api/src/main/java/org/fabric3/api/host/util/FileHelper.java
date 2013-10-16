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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.api.host.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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
     * Returns the index of the last directory separator character.
     * <p/>
     * This method will handle a file in either Unix or Windows format. The position of the last forward or backslash is returned.
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
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
     * Returns the index of the last extension separator character, which is a dot.
     * <p/>
     * This method also checks that there is no directory separator after the last dot. To do this it uses {@link #indexOfLastSeparator(String)} which will
     * handle a file in either Unix or Windows format.
     * <p/>
     * The output will be the same irrespective of the machine that the code is running on.
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
     * Gets the name minus the path from a full filename.
     * <p/>
     * This method will handle a file in either Unix or Windows format. The text after the last forward or backslash is returned.
     * <p/>
     * <pre>
     * a/b/c.txt --&gt; c.txt
     * a.txt     --&gt; a.txt
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; &quot;&quot;
     * </pre>
     * <p/>
     * <p/>
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
     * Gets the extension of a filename.
     * <p/>
     * This method returns the textual part of the filename after the last dot. There must be no directory separator after the dot.
     * <p/>
     * <pre>
     * foo.txt      --&gt; &quot;txt&quot;
     * a/b/c.jpg    --&gt; &quot;jpg&quot;
     * a/b.txt/c    --&gt; &quot;&quot;
     * a/b/c        --&gt; &quot;&quot;
     * </pre>
     * <p/>
     * <p/>
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
     * @throws IOException          if the directory cannot be created
     */
    public static void forceMkdir(File directory) throws IOException {
        if (directory == null) {
            return;
        }
        if (directory.exists()) {
            if (directory.isFile()) {
                String message = "File " + directory + " exists and is " + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (!directory.mkdirs()) {
                String message = "Unable to create directory " + directory;
                throw new IOException(message);
            }
        }
    }

    /**
     * Delete a file. If file is a directory, delete it and all sub-directories.
     * <p/>
     * The difference between File.delete() and this method are: <ul> <li>A directory to be deleted does not have to be empty.</li> <li>You get exceptions when
     * a file or directory cannot be deleted. (java.io.File methods returns a boolean)</li> </ul>
     *
     * @param file file or directory to delete, not null
     * @throws IOException in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            if (!file.delete()) {
                String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * Convert from a <code>URL</code> to a <code>File</code>.
     * <p/>
     * From version 1.1 this method will decode the URL. Syntax such as <code>file:///my%20docs/file.txt</code> will be correctly decoded to <code>/my
     * docs/file.txt</code>.
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
     * Convert from a <code>URL</code> to a <code>File</code>.
     * <p/>
     * From version 1.1 this method will decode the URL. Syntax such as <code>file:///my%20docs/file.txt</code> will be correctly decoded to <code>/my
     * docs/file.txt</code>.
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

    public static FileFilter getFileFilter(String regExp, boolean ignoreCase) {
        return new RegExpFilter(regExp, ignoreCase);
    }

    /**
     * A regular-expression based resource filter
     */
    public static class RegExpFilter implements FileFilter {
        private Pattern pattern;

        public RegExpFilter(Pattern pattern) {
            this.pattern = pattern;
        }

        public RegExpFilter(String patternStr, boolean ignoreCase) {
            this.pattern = Pattern.compile(patternStr, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
        }

        public boolean accept(File file) {
            return pattern.matcher(file.getName()).matches();
        }

        /**
         * Convert wildcard into a regex pattern
         *
         * @param str        the expression
         * @param ignoreCase true if case sensitivity should be ignored
         * @return the regex filter
         */
        public static RegExpFilter getWildcardFilter(String str, boolean ignoreCase) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);
                if (ch == '?') {
                    buffer.append('.');
                } else if (ch == '*') {
                    buffer.append(".*");
                } else {
                    buffer.append(ch);
                }
            }
            return new RegExpFilter(buffer.toString(), ignoreCase);
        }

    }

    /**
     * Clean a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
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
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
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
     * @throws IOException in case cleaning is unsuccessful
     */
    private static void cleanDirectoryOnExit(File directory) throws IOException {
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
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDeleteOnExit(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Copies a whole directory to a new location preserving the file dates.
     * <p/>
     * This method copies the specified directory and all its child directories and files to the specified destination. The destination is the new location and
     * name of the directory.
     * <p/>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     *
     * @param srcDir  an existing directory to copy, must not be <code>null</code>
     * @param destDir the new directory, must not be <code>null</code>
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.1
     */
    public static void copyDirectory(File srcDir, File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    /**
     * Copies a whole directory to a new location.
     * <p/>
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * <p/>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     *
     * @param srcDir           an existing directory to copy, must not be <code>null</code>
     * @param destDir          the new directory, must not be <code>null</code>
     * @param preserveFileDate true if the file date of the copy should be the same as the original
     * @throws IOException if source or destination is invalid
     */
    public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        }
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }
        doCopyDirectory(srcDir, destDir, preserveFileDate);
    }

    /**
     * Copies a directory to within another directory preserving the file dates.
     * <p/>
     * This method copies the source directory and all its contents to a directory of the same name in the specified destination directory.
     * <p/>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     *
     * @param srcDir  an existing directory to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyDirectoryToDirectory(File srcDir, File destDir) throws IOException {
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
     * Copies a file to a new location preserving the file date.
     * <p/>
     * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is created if
     * it does not exist. If the destination file exists, then this method will overwrite it.
     *
     * @param srcFile  an existing file to copy, must not be <code>null</code>
     * @param destFile the new file, must not be <code>null</code>
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    /**
     * Copies a file to a new location.
     * <p/>
     * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is created if
     * it does not exist. If the destination file exists, then this method will overwrite it.
     *
     * @param srcFile          an existing file to copy, must not be <code>null</code>
     * @param destFile         the new file, must not be <code>null</code>
     * @param preserveFileDate true if the file date of the copy should be the same as the original
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        }
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        }
        if (destFile.getParentFile() != null && !destFile.getParentFile().exists()) {
            if (!destFile.getParentFile().mkdirs()) {
                throw new IOException("Destination '" + destFile + "' directory cannot be created");
            }
        }
        if (destFile.exists() && !destFile.canWrite()) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile, preserveFileDate);
    }

    /**
     * Copies a file to a directory preserving the file date.
     * <p/>
     * This method copies the contents of the specified source file to a file of the same name in the specified destination directory. The destination directory
     * is created if it does not exist. If the destination file exists, then this method will overwrite it.
     *
     * @param srcFile an existing file to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    /**
     * Copies a file to a directory optionally preserving the file date.
     * <p/>
     * This method copies the contents of the specified source file to a file of the same name in the specified destination directory. The destination directory
     * is created if it does not exist. If the destination file exists, then this method will overwrite it.
     *
     * @param srcFile          an existing file to copy, must not be <code>null</code>
     * @param destDir          the directory to place the copy in, must not be <code>null</code>
     * @param preserveFileDate true if the file date of the copy should be the same as the original
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.3
     */
    public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) throws IOException {
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
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);
        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Writes an InputStream to disk.
     *
     * @param source the source stream
     * @param target the target disk location
     * @throws IOException if the write encountered an error
     */
    public static void write(InputStream source, File target) throws IOException {
        RandomAccessFile file = new RandomAccessFile(target, "rw");
        FileChannel channel = null;
        FileLock lock = null;
        try {
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
        } finally {
            if (channel != null) {
                if (lock != null) {
                    lock.release();
                }
                channel.close();
            }
            file.close();
        }

    }

    /**
     * Recursively schedule directory for deletion on JVM exit.
     *
     * @param directory directory to delete, must not be <code>null</code>
     * @throws NullPointerException if the directory is <code>null</code>
     * @throws IOException          in case deletion is unsuccessful
     */
    private static void deleteDirectoryOnExit(File directory) throws IOException {
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
     * @throws IOException if an error occurs
     */
    private static void doCopyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            if (preserveFileDate) {
                destDir.setLastModified(srcDir.lastModified());
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        // recurse
        File[] files = srcDir.listFiles();
        if (files == null) { // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
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
     * @throws IOException if an error occurs
     */
    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream input = new FileInputStream(srcFile);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(destFile);
            IOHelper.copy(input, output);
        } finally {
            if (output != null) {
                output.close();
            }
            input.close();
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
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
     * @throws IOException          in case deletion is unsuccessful
     */
    public static void forceDeleteOnExit(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * Given a parentLocation as a base, returns the absolute path to the childLocation. The childLocation may be absolute or relative.  If the child is
     * absolute, it is simply returned unchanged.  If it is relative, this method then resolves the location of the child from the parent location.
     *
     * @param parentLocation
     * @param childLocation
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
     * @param aPath
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
