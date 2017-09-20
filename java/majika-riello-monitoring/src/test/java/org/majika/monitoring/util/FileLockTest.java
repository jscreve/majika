package org.majika.monitoring.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;

public class FileLockTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();


    @Test
    public void testWritingAndReading() throws IOException {
        //create a tmemp file
        File tempFile = folder.newFile();
        String fileName = tempFile.getPath();
        FileLock fileLock = new FileLock(fileName);
        fileLock.writeToFileWithLock("toto");
        String dataReadFromFile = fileLock.readFromFileWithLock();
        Assert.assertTrue(dataReadFromFile.equals("toto"));
    }
}
