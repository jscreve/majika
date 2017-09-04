package org.majika.monitoring.shell;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.majika.monitoring.ftp.FtpHelper;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by etienne on 09/08/2017.
 */
public class ShellManager {

    private static Logger logger = LogManager.getLogger();
    private Properties prop = new Properties();
    private String shellRemoteFolder;
    private String shellResultFile;

    public static void main(String[] args) {
        new ShellManager();
    }

    public ShellManager() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            input.close();
            shellRemoteFolder =  prop.getProperty("shellRemoteFolder");
            shellResultFile =  prop.getProperty("shellResultFile");
        }  catch (IOException ex) {
            logger.error("Could not load properties file", ex);
        }
    }

    public void executeShellCommand() {
        FTPClient ftpClient = null;
        try {
            ftpClient = FtpHelper.connectToFTP(prop);
            ftpClient.enterLocalPassiveMode();
            FTPFile[] ftplist = ftpClient.listFiles(shellRemoteFolder);
            //retrieve command
            for (FTPFile ftpfile : ftplist) {
                if (ftpfile.isFile() && !ftpfile.getName().equals(shellResultFile)) {
                    String remoteFile1 = shellRemoteFolder + ftpfile.getName();
                    String command = FtpHelper.getRemoteFileContent(ftpClient, remoteFile1);
                    ftpClient.deleteFile(remoteFile1);
                    String outputCommand = executeCommand(command);
                    String remoteShellOutput = shellRemoteFolder + shellResultFile;
                    //TODO investigate why reconnection is required to store file
                    FtpHelper.disconnectFTP(ftpClient);
                    ftpClient = FtpHelper.connectToFTP(prop);
                    FtpHelper.storeRemoteFile(ftpClient, outputCommand, remoteShellOutput);
                }
            }
        } catch (IOException ex) {
            logger.error("Error in ftp connection", ex);
        } finally {
            try {
                if (ftpClient != null && ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                logger.error("Error in ftp disconnection", ex);
            }
        }
    }

    private String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            logger.error(e);
        }

        return output.toString();
    }

    public String getShellRemoteFolder() {
        return shellRemoteFolder;
    }

    public void setShellRemoteFolder(String shellRemoteFolder) {
        this.shellRemoteFolder = shellRemoteFolder;
    }

    public String getShellResultFile() {
        return shellResultFile;
    }

    public void setShellResultFile(String shellResultFile) {
        this.shellResultFile = shellResultFile;
    }

}
