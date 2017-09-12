package org.majika.monitoring.updateProgram;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.majika.monitoring.ftp.FtpHelper;

import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * Created by etienne on 17/08/2017.
 */
public class ProgramUpdate {

    private static Logger logger = LogManager.getLogger(ProgramUpdate.class);
    private Properties prop = new Properties();
    private String programUpdateRemoteFolder;
    private String successProgramUploadFileName;
    private String jarPath;

    public ProgramUpdate() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            programUpdateRemoteFolder = prop.getProperty("programUpdateRemoteFolder");
            successProgramUploadFileName = prop.getProperty("successProgramUploadFileName");
            jarPath = prop.getProperty("jarPath");
        }  catch (IOException ex) {
            logger.error("Could not load properties file", ex);
        }
    }

    public void executeUpdateCommand() {
        FTPClient ftpClient = null;
        Date date = new Date();
        try {
            ftpClient = FtpHelper.connectToFTP(prop);
            FTPFile[] ftplist = ftpClient.listFiles(programUpdateRemoteFolder);
            String infoDownloading = "Files ";
            for (FTPFile ftpfile : ftplist) {
                if (ftpfile.isFile() && !ftpfile.getName().equals(successProgramUploadFileName)) {
                    String remoteFile1 = programUpdateRemoteFolder + ftpfile.getName();
                    File downloadJar = new File(jarPath + ftpfile.getName());
                    OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadJar));
                    logger.info("Start Downloading File : " +ftpfile.getName());
                    boolean successJar = ftpClient.retrieveFile(remoteFile1, outputStream1);
                    outputStream1.close();
                    ftpClient.deleteFile(remoteFile1);
                    infoDownloading = infoDownloading + ":" + ftpfile.getName();

                    if (successJar) {
                        logger.info(ftpfile.getName() + " have been downloaded successfully.");
                    }
                }
            }
            if (!infoDownloading.equals("Files ")) {
                try {
                    FileWriter fw = new FileWriter(jarPath + successProgramUploadFileName);
                    BufferedWriter fileDownloaded = new BufferedWriter(fw);
                    fileDownloaded.write(infoDownloading + " has been downloaded on raspberry Pi successfully, date : " + date);
                    fileDownloaded.close();
                    fw.close();

                    File firstLocalFile = new File(jarPath + successProgramUploadFileName);
                    String firstRemoteFile = programUpdateRemoteFolder + successProgramUploadFileName;
                    InputStream inputStream = new FileInputStream(firstLocalFile);
                    logger.info("Start uploading Dwn file");
                    ftpClient.storeFile(firstRemoteFile, inputStream);
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("Problem to create Dowloading file", e);
                }
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
        new ProgramUpdate();
    }
}
