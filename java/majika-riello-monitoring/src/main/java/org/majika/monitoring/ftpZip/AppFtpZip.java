package org.majika.monitoring.ftpZip;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.majika.monitoring.ftp.FtpHelper;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by etienne on 09/08/2017.
 */
public class AppFtpZip {

    private static Logger logger = LogManager.getLogger(AppFtpZip.class);
    private Properties prop = new Properties();
    private String csvZipRemoteDirectory;
    private int retryCounter = 0;

    public static void main(String[] args) {
        new AppFtpZip();
    }

    public AppFtpZip() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            input.close();
            csvZipRemoteDirectory = prop.getProperty("csvZipRemoteDirectory");
        }  catch (IOException ex) {
            logger.error("Could not load properties file", ex);
        }
    }

    public void executeFtpZipCommand() {
        FTPClient ftpClient = null;
        Zip zip = new Zip();
        Date date = new Date();
        SimpleDateFormat zipFormat = new SimpleDateFormat("dd_MM_yyyy");
        SimpleDateFormat folderMonthly = new SimpleDateFormat("MM_yyyy");
        try {
            ftpClient = FtpHelper.connectToFTP(prop);
            String dirMonthly = folderMonthly.format(date).toString();
            if (!ftpClient.changeWorkingDirectory(csvZipRemoteDirectory + dirMonthly)) {
                ftpClient.makeDirectory(csvZipRemoteDirectory + dirMonthly);
            }
            // APPROACH #1: uploads first file using an InputStream
            File firstLocalFile = new File(zip.getOrCreateZipFile());
            String firstRemoteFile = csvZipRemoteDirectory + dirMonthly + "/" + zipFormat.format(date).toString() + ".zip";
            InputStream inputStream = new FileInputStream(firstLocalFile);
            logger.info("Start uploading first file");
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                logger.info("File Uploaded");
            }
        } catch (IOException ex) {
            logger.error("Ftp connection failed", ex);
            //retry mechanism
            retryCounter++;
            if(retryCounter <= 3) {
                logger.error("Retry ftp sending");
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                }
                executeFtpZipCommand();
            } else {
                logger.error("Do not retry ftp sending, max counter is reached");
                retryCounter = 0;
            }
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                logger.error("Disconnection FTP", ex);
            }
        }
    }

}
