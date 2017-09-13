package org.majika.monitoring.command;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.majika.monitoring.ftp.FtpHelper;
import org.majika.monitoring.util.CommandHelper;

import java.io.*;
import java.util.Properties;

/**
 * Created by etienne on 09/08/2017.
 */
public class CommandManager {

    private static Logger logger = LogManager.getLogger(CommandManager.class);
    private Properties prop = new Properties();
    private String commandRemoteFolder;
    private String commandResultFile;
    private static final String SHELL_COMMAND = "shell:";

    public static void main(String[] args) {
        new CommandManager();
    }

    public CommandManager() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            input.close();
            commandRemoteFolder =  prop.getProperty("commandRemoteFolder");
            commandResultFile =  prop.getProperty("commandResultFile");
        }  catch (IOException ex) {
            logger.error("Could not load properties file", ex);
        }
    }

    public void executeCommand() {
        FTPClient ftpClient = null;
        try {
            ftpClient = FtpHelper.connectToFTP(prop);
            ftpClient.enterLocalPassiveMode();
            FTPFile[] ftplist = ftpClient.listFiles(commandRemoteFolder);
            //retrieve command
            for (FTPFile ftpfile : ftplist) {
                if (ftpfile.isFile() && !ftpfile.getName().equals(commandResultFile)) {
                    String remoteFile1 = commandRemoteFolder + ftpfile.getName();
                    String command = FtpHelper.getRemoteFileContent(ftpClient, remoteFile1);
                    ftpClient.deleteFile(remoteFile1);
                    logger.info("Running remote command : " + command + " from file : " + ftpfile.getName());
                    //check command type
                    String outputCommand = "";
                    if(command.startsWith(SHELL_COMMAND)) {
                        String shellCommand = command.substring(command.indexOf(SHELL_COMMAND) + SHELL_COMMAND.length());
                        outputCommand = CommandHelper.executeCommand(shellCommand);
                    }
                    String remoteCommandOutput = commandRemoteFolder + commandResultFile;
                    //TODO investigate why reconnection is required to store file
                    //FtpHelper.disconnectFTP(ftpClient);
                    //ftpClient = FtpHelper.connectToFTP(prop);
                    FtpHelper.storeRemoteFile(ftpClient, outputCommand, remoteCommandOutput);
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

    public String getCommandRemoteFolder() {
        return commandRemoteFolder;
    }

    public void setCommandRemoteFolder(String commandRemoteFolder) {
        this.commandRemoteFolder = commandRemoteFolder;
    }

    public String getCommandResultFile() {
        return commandResultFile;
    }

    public void setCommandResultFile(String commandResultFile) {
        this.commandResultFile = commandResultFile;
    }

}
