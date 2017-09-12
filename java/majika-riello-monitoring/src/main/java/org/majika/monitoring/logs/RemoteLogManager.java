package org.majika.monitoring.logs;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.majika.monitoring.ftp.FtpHelper;
import org.majika.monitoring.util.CommandHelper;

import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * Created by etienne on 17/08/2017.
 */
public class RemoteLogManager {

    private static Logger logger = LogManager.getLogger(RemoteLogManager.class);
    //private static Logger statConnectionLogger = LogManager.getLogger("monitoring.stat");
    private static final Marker STAT_MARKET = MarkerManager.getMarker("STAT");
    private Properties prop = new Properties();
    private String ftpLogFolder;
    private String connectionInfoCommand;

    public RemoteLogManager() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            ftpLogFolder = prop.getProperty("ftpLogFolder");
            connectionInfoCommand = prop.getProperty("connectionInfos");
        } catch (IOException ex) {
            logger.error("Could not load properties file", ex);
        }
    }

    public void writeConnectionStat() {
        String connectionInfo = CommandHelper.executeCommand(connectionInfoCommand);
        //only error level is written
        logger.info(STAT_MARKET,"Connection stat : " + connectionInfo);
    }

    public void sendLogs() {
        FTPClient ftpClient = null;
        try {
            ftpClient = FtpHelper.connectToFTP(prop);
            //envoi des logs ftp
            File ftpLogFile = new File(prop.getProperty("ftpLogPath") + prop.getProperty("ftpLogFile"));
            if (ftpLogFile.exists()) {
                InputStream ftpLogFileInputStream = new BufferedInputStream(new FileInputStream(ftpLogFile));
                logger.info("Start uploading ftp log file");
                String remoteFtpLogFile = ftpLogFolder + prop.getProperty("ftpLogFile");
                ftpClient.storeFile(remoteFtpLogFile, ftpLogFileInputStream);
                ftpLogFileInputStream.close();
            }
        } catch (IOException ex) {
            logger.error("Connection Internet off", ex);
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

    public static void main(String[] args) {
        new RemoteLogManager();
    }
}
