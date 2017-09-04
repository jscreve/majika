package org.majika.monitoring.ftp;

import java.io.*;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by etienne on 01/08/2017.
 */
public class AppFtp {

    private static Logger logger = LogManager.getLogger(AppFtp.class);
    private Properties prop = new Properties();
    private String jsonRemoteDirectory;
    private String jsonFileName;

    public static void main(String[] args) {
        new AppFtp();
    }

    /**
     *
     */
    public AppFtp() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            jsonRemoteDirectory = prop.getProperty("jsonRemoteDirectory");
            jsonFileName = prop.getProperty("jsonFileName");
        }  catch (IOException ex) {
            logger.error("Could not load properties file", ex);
        }
    }

    public void executeFTPCommand() {
        FTPClient ftpClient = null;
        try{Thread.sleep(5000);}catch(Exception e){logger.error("Couldnt wait",e);}
        FileJsonFtp fJs = new FileJsonFtp();
        fJs.setFileJson();
        try {
            ftpClient = FtpHelper.connectToFTP(prop);
            // APPROACH #1: uploads first file using an InputStream
            File firstLocalFile = new File(prop.getProperty("pathDirFile") + jsonFileName);
            String firstRemoteFile = jsonRemoteDirectory + jsonFileName;
            InputStream inputStream = new FileInputStream(firstLocalFile);

            logger.info("Start uploading Json file");
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                logger.info("File Uploaded");
            }
        } catch (IOException ex) {
            logger.error("Ftp connection failed", ex);
            //Unless it works otherwise
            //try{Runtime.getRuntime().exec("sudo /usr/bin/modem3g/sakis3g connect OTHER=\"USBMODEM\" USBMODEM=\"12d1:1506\" USBINTERFACE=\"0\"");}catch(IOException h){logger.error("USB Modem bugging",h);}
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
