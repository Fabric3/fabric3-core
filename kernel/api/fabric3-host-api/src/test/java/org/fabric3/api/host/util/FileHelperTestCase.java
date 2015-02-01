package org.fabric3.api.host.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import junit.framework.TestCase;
import org.fabric3.api.host.Fabric3Exception;

/**
 *
 */
public class FileHelperTestCase extends TestCase {

    private String tmpPath;
    private final char fileSep = File.separatorChar;

    public FileHelperTestCase() {
        super();
        String tmpDirName = System.getProperty("java.io.tmpdir");
        File tmp = new File(tmpDirName);
        String absolutePath = tmp.getAbsolutePath();
		tmpPath = absolutePath+fileSep+"f3";
        File f = new File(tmpPath);
        f.mkdirs();
        f.deleteOnExit();
    }

    public void testIndexOfLastSeparator() throws Exception {
        String fileName = null;
        assertEquals(-1,FileHelper.indexOfLastSeparator(fileName));
        fileName = "f3/test";
        assertEquals(2,FileHelper.indexOfLastSeparator(fileName));
        fileName = "test\\f3\\path";
        assertEquals(7, FileHelper.indexOfLastSeparator(fileName));
        fileName = "test//f3//path";
        assertEquals(9, FileHelper.indexOfLastSeparator(fileName));

    }

    public void testIndexOfExtension() throws Exception {
        String fileName = null;
        assertEquals(-1, FileHelper.indexOfExtension(fileName));
        fileName = "f3.test";
        assertEquals(2,FileHelper.indexOfExtension(fileName));
        fileName = "f3.test.tmp";
        assertEquals(7,FileHelper.indexOfExtension(fileName));
        fileName = "f3";
        assertEquals(-1,FileHelper.indexOfExtension(fileName));
    }

    public void testGetName() throws Exception {
        String fileName = null;
        assertNull(FileHelper.getName(fileName));
        fileName = "f3.tmp";
        assertEquals(fileName,FileHelper.getName(fileName));
        String path = "a/b/c/d/";
        assertEquals(fileName,FileHelper.getName(path + fileName));
        path = "a\\b\\c\\d\\";
        assertEquals(fileName, FileHelper.getName(path + fileName));
        path= "a//b//c//d//";
        assertEquals(fileName, FileHelper.getName(path + fileName));
    }

    public void testGetExtension() throws Exception {
        String fileName = null;
        assertNull(FileHelper.getExtension(fileName));
        fileName = "f3.tmp";
        assertEquals("tmp",FileHelper.getExtension(fileName));
        fileName = "f3/tmp/test.csv";
        assertEquals("csv", FileHelper.getExtension(fileName));
        fileName = "f3-no-extension";
        assertEquals("", FileHelper.getExtension(fileName));
    }

    public void testForceMkdir() throws Exception {
        File tmpDir = null;

        // empty directory should not throw an exception
        FileHelper.forceMkdir(tmpDir);

        tmpDir = new File(tmpPath+"/force/create/path");
        FileHelper.forceMkdir(tmpDir);
        assertTrue(tmpDir.exists());
        forceDelete(tmpDir);
    }

    public void testForceDelete() throws Exception {
        //String tmpPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpPath + "/force/create/path");
        FileHelper.forceMkdir(tmpDir);
        FileHelper.forceDelete(tmpDir);
        assertFalse(tmpDir.exists());
        forceDelete(tmpDir.getParentFile().getParentFile());
    }

    public void testToFile() throws Exception {
        URL url = null;
        assertNull(FileHelper.toFile(url));
        url = new URL("file:///"+tmpPath+"/my%20docs/file.txt");
        assertEquals(tmpPath + fileSep + "my docs" + fileSep + "file.txt", FileHelper.toFile(url).getAbsolutePath());
        url = new URL("ftp:///should/be/null");
        assertNull(FileHelper.toFile(url));
    }

    public void testToFileString() throws Exception {
        URL url = null;
        assertNull(FileHelper.toFile(url));
        url = new URL("file:///my%20docs/file.txt");
        assertEquals(fileSep +"my docs"+fileSep+"file.txt", FileHelper.toFileString(url));
        url = new URL("ftp:///should/be/null");
        assertNull(FileHelper.toFile(url));
    }

    public void testCleanDirectory() throws Exception {
        File clean = null;
        FileHelper.cleanDirectory(clean);

        clean = new File(tmpPath+"/clean");
        FileHelper.forceMkdir(clean);
        File tmpFile = File.createTempFile("f3-clean", "txt", clean);
        assertTrue(tmpFile.exists());

        FileHelper.cleanDirectory(clean);
        assertFalse(tmpFile.exists());
        assertTrue(clean.exists());

        FileHelper.deleteDirectory(clean);

    }

    public void testCopyDirectory() throws Exception {
        File sourceDir = new File(tmpPath+"/sourceDir");
        File destDir = new File(tmpPath, "destDir");

        FileHelper.deleteDirectory(destDir);
        FileHelper.deleteDirectory(sourceDir);

        FileHelper.forceMkdir(sourceDir);

        addTmpFiles(sourceDir,"sourceFile");

        File sourceSubDir = new File(sourceDir,"subdir");
        FileHelper.forceMkdir(sourceSubDir);
        addTmpFiles(sourceSubDir,"subsubFiles");

        assertEquals(11, sourceDir.list().length);
        assertEquals(10,sourceSubDir.list().length);

        FileHelper.forceMkdir(destDir);
        File destDirSub = new File(destDir, "subdir");
        FileHelper.copyDirectory(sourceDir,destDir);
        assertEquals(11,destDir.list().length);
        assertEquals(10,destDirSub.list().length);

        FileHelper.forceDelete(destDir);
        assertFalse(destDir.exists());

        long lastModified = System.currentTimeMillis() - 1000000;
        Map<String, Long> tstampMap = new HashMap<>(sourceDir.list().length);

        for (File file : sourceDir.listFiles()) {
            if(file.isFile()){
                file.setLastModified(lastModified);
                tstampMap.put(file.getName(),file.lastModified());
            }
        }

        FileHelper.copyDirectory(sourceDir,destDir,true);
        for (File file : destDir.listFiles()) {
            if(file.isFile())
                assertEquals(tstampMap.get(file.getName()).longValue(), file.lastModified());
        }

        FileHelper.forceDelete(destDir);

        FileHelper.copyDirectory(sourceDir,destDir,false);
        for (File file : destDir.listFiles()) {
            if (file.isFile())
                assertFalse(lastModified == file.lastModified());
        }

        FileHelper.forceDelete(destDir);
        FileHelper.forceDelete(sourceDir);
    }

    private void addTmpFiles(File directory, String prefix) throws IOException {
        for (int i = 0; i < 10; i++)
            File.createTempFile(prefix, null, directory);
    }

    private void forceDelete(File ... files) throws IOException {
        for (File file : files) {
            if(file.exists())
                FileHelper.forceDelete(file);
        }
    }

    private void forceMkdir(File ... files) throws IOException {
        for (File file : files) {
            FileHelper.forceMkdir(file);
        }
    }

    private void setUpDirs(File ... files) throws IOException {
        forceDelete(files);
        forceMkdir(files);
    }

    public void testCopyDirectoryToDirectory() throws Exception {
        File srcDir = new File(tmpPath+"/srcDir");
        File destDir = new File(tmpPath + "/destDir");

        setUpDirs(srcDir, destDir);

        addTmpFiles(srcDir, "srcFile");


        FileHelper.copyDirectoryToDirectory(srcDir, destDir);
        assertEquals(1, destDir.list().length);
        assertEquals(10, destDir.listFiles()[0].list().length);

        forceDelete(srcDir,destDir);
    }

    public void testCopyFile() throws Exception {
        File srcDir = new File(tmpPath+"/src");
        File destFile = new File(tmpPath+"/dest/destFile");

        setUpDirs(srcDir);

        File srcFile = File.createTempFile("srcFile",null,srcDir);
        FileHelper.copyFile(srcFile, destFile);

        assertTrue(destFile.exists());

        forceDelete(srcDir,destFile.getParentFile());
    }

    public void testCopyFileToDirectory() throws Exception {
        File srcDir = new File(tmpPath+"/srcDir/");
        File destDir = new File(tmpPath +"/destDir");
        setUpDirs(srcDir, destDir);
        File srcFile = File.createTempFile("srcFile", null, srcDir);

        FileHelper.copyFileToDirectory(srcFile, destDir);
        assertEquals(1, destDir.list().length);

        try {
            FileHelper.copyFileToDirectory(srcDir,destDir);
            fail("Should not be able to copy a Directory");
        } catch (Fabric3Exception e) {
            // everything is ok
        }

        srcFile.setLastModified(System.currentTimeMillis()-1000000);

        long modified = srcFile.lastModified();

        setUpDirs(destDir);
        FileHelper.copyFileToDirectory(srcFile,destDir,false);
        assertFalse(modified == destDir.listFiles()[0].lastModified());

        FileHelper.copyFileToDirectory(srcFile, destDir, true);
        assertTrue(modified == destDir.listFiles()[0].lastModified());

        forceDelete(srcDir, destDir);
    }

    public void testDeleteDirectory() throws Exception {
        File testDir = new File(tmpPath+"testDelete");
        setUpDirs(testDir);
        FileHelper.deleteDirectory(testDir);
        assertFalse(testDir.exists());
        setUpDirs(testDir);
        addTmpFiles(testDir,"testDelete");
        FileHelper.deleteDirectory(testDir);
        assertFalse(testDir.exists());
    }

    public void testWrite() throws Exception {
        String testData = "this is the test data";
        ByteArrayInputStream stream = new ByteArrayInputStream(testData.getBytes("UTF-8"));
        File destFile = new File(tmpPath+"/testFile");
        FileHelper.write(stream,destFile);
        Scanner scanner = new Scanner(destFile,"UTF-8");
        String returned = scanner.nextLine();
        assertEquals(testData,returned);
        scanner.close();
        forceDelete(destFile);
    }

    public void testResolveRelativePath() throws Exception {
        String parent = "/usr/dev/f3/";
        String child = "test";

        assertEquals(parent+child,FileHelper.resolveRelativePath(parent,child));
        child = "../test";
        assertEquals("/usr/dev/test",FileHelper.resolveRelativePath(parent,child));
        child = "test/sub/test";
        assertEquals(parent + child, FileHelper.resolveRelativePath(parent, child));
        child = null;
        assertNull(FileHelper.resolveRelativePath(parent, child));
        child = "test";
        parent = null;
        assertEquals(child,FileHelper.resolveRelativePath(parent,child));
        parent = "/usr/dev/f3/";
        child = "/test/test";
        assertEquals(child, FileHelper.resolveRelativePath(parent, child));

        parent = "C:\\usr\\dev\\f3\\";
        child = "test";
        assertEquals("C:/usr/dev/f3/test", FileHelper.resolveRelativePath(parent, child));
    }

    public void testIsAbsolute() throws Exception {
        assertTrue(FileHelper.isAbsolute("/usr/dev/projects"));
        assertFalse(FileHelper.isAbsolute("usr/dev/projects"));
        assertTrue(FileHelper.isAbsolute("C:\\test"));
        assertTrue(FileHelper.isAbsolute("C://test"));
        assertTrue(FileHelper.isAbsolute("C:/test"));
        assertFalse(FileHelper.isAbsolute("test\\blah\\blah"));
    }
}
