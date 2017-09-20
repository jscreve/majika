package org.majika.monitoring.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;

/**
 * Helper class to read and write concurrently on a file. Methods are blockings
 */
public class FileLock {

    private static Logger logger = LogManager.getLogger(FileLock.class);

    private String fileName;

    public FileLock(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
    }

    /**
     * Blocking writing function
     * @param data to write to file
     */
    public void writeToFileWithLock(String data) throws FileNotFoundException {
        ByteBuffer buffer = null;
        try (RandomAccessFile  randomAccessFile = new RandomAccessFile(fileName, "rw");
             FileChannel fc = randomAccessFile.getChannel();
             java.nio.channels.FileLock fileLock = fc.lock()) {
            if (null != fileLock) {
                buffer = ByteBuffer.wrap(data.getBytes());
                buffer.put(data.toString().getBytes());
                buffer.flip();
                while (buffer.hasRemaining())
                    fc.write(buffer);
            }
        } catch (OverlappingFileLockException | IOException ex) {
            logger.error("Exception occured while trying to get a lock on File... " + ex.getMessage());
        }
    }

    /**
     * Blocking reading function
     * @return the data read from file
     */
    public String readFromFileWithLock() throws FileNotFoundException {
        String output = "";
        try (RandomAccessFile  randomAccessFile = new RandomAccessFile(fileName, "rw");
             FileChannel fc = randomAccessFile.getChannel();
             java.nio.channels.FileLock fileLock = fc.lock()) {
            if (null != fileLock) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int bytes = fc.read(byteBuffer);
                while(bytes != -1){
                    byteBuffer.flip();
                    while (byteBuffer.hasRemaining()){
                        output += (char)byteBuffer.get();
                    }
                    byteBuffer.clear();
                    bytes = fc.read(byteBuffer);
                }
            }
        } catch (OverlappingFileLockException | IOException ex) {
            logger.error("Exception occured while trying to get a lock on File... " + ex.getMessage());
        }
        return output;
    }
}
