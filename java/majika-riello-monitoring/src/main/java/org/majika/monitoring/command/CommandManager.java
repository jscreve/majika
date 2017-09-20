package org.majika.monitoring.command;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.majika.monitoring.ftp.FtpHelper;
import org.majika.monitoring.ftpZip.AppFtpZip;
import org.majika.monitoring.util.CommandHelper;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by etienne on 09/08/2017.
 */
public class CommandManager {

    private static Logger logger = LogManager.getLogger(CommandManager.class);
    private Properties prop = new Properties();
    private AppFtpZip appFtpZip = new AppFtpZip();
    private String commandRemoteFolder;
    private String commandResultFile;
    private static final String SHELL_COMMAND = "shell:";
    private static final String FTPZIP_COMMAND = "ftpzip:";
    private static SimpleDateFormat ftpzipFormat = new SimpleDateFormat("dd_MM_yyyy");

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
                    } else if(command.startsWith(FTPZIP_COMMAND)) {
                        String ftpzipCommand = command.substring(command.indexOf(FTPZIP_COMMAND) + FTPZIP_COMMAND.length());
                        //get Date
                        try {
                            Date date = ftpzipFormat.parse(ftpzipCommand);
                            appFtpZip.init(date);
                            appFtpZip.executeFtpZipCommand();
                            outputCommand = ftpzipCommand;
                        } catch (ParseException e) {
                            logger.error("Could not parse date in command : " + ftpzipCommand);
                        }
                    }
                    String remoteCommandOutput = commandRemoteFolder + commandResultFile;
                    //TODO investigate why reconnection is required to store file
                    FtpHelper.disconnectFTP(ftpClient);
                    ftpClient = FtpHelper.connectToFTP(prop);
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

    public AppFtpZip getAppFtpZip() {
        return appFtpZip;
    }

    public void setAppFtpZip(AppFtpZip appFtpZip) {
        this.appFtpZip = appFtpZip;
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
