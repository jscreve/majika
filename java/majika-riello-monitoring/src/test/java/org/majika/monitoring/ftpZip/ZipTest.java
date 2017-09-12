package org.majika.monitoring.ftpZip;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class ZipTest {

    private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(ZipTest.class);

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Before
    public void init() throws IOException {
    }

    @Test
    public void testCreateZipFile() throws IOException {
        Zip zip = new Zip();
        String tempFolderPath = folder.newFolder().getPath() + "/";
        zip.setPathDir(tempFolderPath);
        File subFolder = new File(zip.getZipDir(tempFolderPath));
        subFolder.mkdirs();

        String zipPath = zip.getOrCreateZipFile();
        File file = new File(zipPath);
        Assert.assertTrue(file.exists());
    }

    @Test
    public void testGetCreatedZipFile() throws IOException {
        Zip zip = new Zip();
        String tempFolderPath = folder.newFolder().getPath() + "/";
        zip.setPathDir(tempFolderPath);
        File subFolder = new File(zip.getZipDir(tempFolderPath));
        subFolder.mkdirs();

        String zipPath = zip.getOrCreateZipFile();
        File file1 = new File(zipPath);
        Assert.assertTrue(file1.exists());

        zipPath = zip.getOrCreateZipFile();
        File file2 = new File(zipPath);
        logger.info("file date : " + file2.lastModified());
        Assert.assertTrue(file2.lastModified() == file1.lastModified());
    }
}
