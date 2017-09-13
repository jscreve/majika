package org.majika.monitoring.ftpZip;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
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
    private Zip zip = new Zip();
    private FTPClient ftpClient = null;
    private Date date;
    private SimpleDateFormat zipFormat = new SimpleDateFormat("dd_MM_yyyy");
    private SimpleDateFormat folderMonthly = new SimpleDateFormat("MM_yyyy");
    private String dirMonthly;

    public static void main(String[] args) {
        new AppFtpZip();
    }

    public AppFtpZip() {
    }

    public void init(Date inputDate) {
        try {
            date = inputDate;
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            input.close();
            csvZipRemoteDirectory = prop.getProperty("csvZipRemoteDirectory");
            dirMonthly = folderMonthly.format(date).toString();
        } catch (IOException ex) {
            logger.error("Could not load properties file", ex);
        }
    }

    public String getRemoteFileName() {
        String remoteFileName = csvZipRemoteDirectory + dirMonthly + "/" + zipFormat.format(date).toString() + ".zip";
        return remoteFileName;
    }

    public void executeFtpZipCommand() {
        try {
            ftpClient = FtpHelper.connectToFTP(prop);
            if (!FtpHelper.isSubDirectory(ftpClient, csvZipRemoteDirectory, dirMonthly)) {
                ftpClient.makeDirectory(csvZipRemoteDirectory + dirMonthly);
            }
            File firstLocalFile = new File(zip.getOrCreateZipFile(date));
            String firstRemoteFile = getRemoteFileName();
            InputStream inputStream = new FileInputStream(firstLocalFile);
            logger.info("Start uploading first file");
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                logger.info("File uploaded");
            } else {
                logger.error("File not uploaded");
            }
        } catch (IOException ex) {
            logger.error("Error in execute FtpZip command", ex);
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

    public Zip getZip() {
        return zip;
    }

    public void setZip(Zip zip) {
        this.zip = zip;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public Properties getProp() {
        return prop;
    }

    public void setProp(Properties prop) {
        this.prop = prop;
    }

    public String getCsvZipRemoteDirectory() {
        return csvZipRemoteDirectory;
    }

    public void setCsvZipRemoteDirectory(String csvZipRemoteDirectory) {
        this.csvZipRemoteDirectory = csvZipRemoteDirectory;
    }
}
