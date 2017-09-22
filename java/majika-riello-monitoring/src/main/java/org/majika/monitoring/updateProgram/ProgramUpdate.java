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
            String infoDownloading = "Files ";
            infoDownloading = copyFtpFiles(ftpClient, programUpdateRemoteFolder, jarPath, infoDownloading);
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
        } catch (Exception ex) {
            logger.error("Error updating program", ex);
        } finally {
            FtpHelper.disconnectFTP(ftpClient);
        }
    }

    public String copyFtpFiles(FTPClient ftpClient, String remoteFolder, String localPath, String infoDownloading) throws IOException {
        FTPFile[] ftplist = ftpClient.listFiles(remoteFolder);
        for (FTPFile ftpfile : ftplist) {
            //copy files
            if (ftpfile.isFile() && !ftpfile.getName().equals(successProgramUploadFileName)) {
                String remoteFile1 = remoteFolder + ftpfile.getName();
                File downloadJar = new File(localPath + ftpfile.getName());
                OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadJar));
                logger.info("Start Downloading File : " +ftpfile.getName());
                boolean successJar = ftpClient.retrieveFile(remoteFile1, outputStream1);
                outputStream1.close();
                ftpClient.deleteFile(remoteFile1);
                infoDownloading = infoDownloading + ":" + ftpfile.getName();

                if (successJar) {
                    logger.info(ftpfile.getName() + " have been downloaded successfully.");
                }
            } else if(ftpfile.isDirectory() && !ftpfile.getName().equals(".") && !ftpfile.getName().equals("..")) {
                //recursive call
                //create local dir if required
                String newDir = localPath + ftpfile.getName();
                new File(newDir).mkdir();
                infoDownloading += copyFtpFiles(ftpClient, remoteFolder + ftpfile.getName() + "/", localPath + ftpfile.getName() + "/", infoDownloading);
            }
        }
        return infoDownloading;
    }

    public String getProgramUpdateRemoteFolder() {
        return programUpdateRemoteFolder;
    }

    public void setProgramUpdateRemoteFolder(String programUpdateRemoteFolder) {
        this.programUpdateRemoteFolder = programUpdateRemoteFolder;
    }

    public String getSuccessProgramUploadFileName() {
        return successProgramUploadFileName;
    }

    public void setSuccessProgramUploadFileName(String successProgramUploadFileName) {
        this.successProgramUploadFileName = successProgramUploadFileName;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public static void main(String[] args) {
        new ProgramUpdate();
    }
}
