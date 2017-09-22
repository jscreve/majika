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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by etienne on 09/08/2017.
 */
public class CommandManager {

    private static Logger logger = LogManager.getLogger(CommandManager.class);
    private Properties prop = new Properties();
    private AppFtpZip appFtpZip = new AppFtpZip();
    private String commandRemoteFolder;
    private String commandResultFile;
    private CommandHelper commandHelper;

    public String getThisCommand() {
        return thisCommand;
    }

    public void setThisCommand(String thisCommand) {
        this.thisCommand = thisCommand;
    }

    private String thisCommand;
    private static final String SHELL_COMMAND = "shell:";
    private static final String FTPZIP_COMMAND = "ftpzip:";
    private static final String GETLOG_COMMAND = "getlog";
    private static final String UPDATE_CRON_COMMAND = "cron";
    private static SimpleDateFormat ftpzipFormat = new SimpleDateFormat("dd_MM_yyyy");

    public static void main(String[] args) {
        CommandManager commandManager = new CommandManager();
        //commandManager.executeCronCommand("cron:update:csv:*/2 * * *");
        //System.out.println(commandManager.executeCronCommand("cron:add:titi:*/2 * * * *"));
        //commandManager.executeCronCommand("cron:remove:csv");
        commandManager.setThisCommand("/Users/jscreve/Documents/GitHub/majika-monitoring/java/majika-riello-monitoring/build/distributions/majika-riello-monitoring-1.0/bin/majika-riello-monitoring");
        System.out.println(commandManager.excuteGetLogCommand());

    }

    public CommandManager() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            input.close();
            commandRemoteFolder =  prop.getProperty("commandRemoteFolder");
            commandResultFile =  prop.getProperty("commandResultFile");
            thisCommand = prop.getProperty("thisCommand");
            commandHelper = new CommandHelper();
        }  catch (IOException ex) {
            logger.error("Could not load properties file", ex);
        }
    }

    public void executeCommandFromFTP() {
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
                    String outputCommand = internalExecuteCommand(command);
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

    protected String internalExecuteCommand(String command) {
        //check command type
        String outputCommand = "";
        //format: shell:unixCommand
        if(command.startsWith(SHELL_COMMAND)) {
            String shellCommand = command.substring(command.indexOf(SHELL_COMMAND) + SHELL_COMMAND.length());
            outputCommand = commandHelper.executeCommand(shellCommand);

            //format : ftpzip:dd_MM_YYYY
        } else if(command.startsWith(FTPZIP_COMMAND)) {
            outputCommand = executeFtpZipCommand(command);

            //format : getlog
        } else if (command.startsWith(GETLOG_COMMAND)) {
            outputCommand = excuteGetLogCommand();

            //format :
            // 1) cron:update:command:frequency
            // 2) cron:add:command:frequency
            // 3) cron:remove:command
        } else if (command.startsWith(UPDATE_CRON_COMMAND)) {
            outputCommand = executeCronCommand(command);
        }
        return outputCommand;
    }

    private String executeFtpZipCommand(String inputCommand) {
        String outputCommand = "";
        String ftpzipCommand = inputCommand.substring(inputCommand.indexOf(FTPZIP_COMMAND) + FTPZIP_COMMAND.length());
        //get Date
        try {
            Date date = ftpzipFormat.parse(ftpzipCommand);
            appFtpZip.init(date);
            appFtpZip.executeFtpZipCommand();
            outputCommand = ftpzipCommand;
        } catch (ParseException e) {
            logger.error("Could not parse date in command : " + ftpzipCommand);
        }
        return outputCommand;
    }

    private String excuteGetLogCommand() {
        String commandToExecute = thisCommand + " uploadLogs";
        String outputCommand = commandHelper.executeCommand(commandToExecute);
        return outputCommand;
    }

    // 1) cron:update:command:frequency
    // 2) cron:add:command:frequency
    // 3) cron:remove:command
    private String executeCronCommand(String inputCommand) {
        String outputCommand = "";

        if(inputCommand.contains("cron:update")) {
            // 1) cron:update:command:frequency
            Pattern p = Pattern.compile("cron:update:(.*):(.*)");
            Matcher m = p.matcher(inputCommand);
            boolean b = m.matches();
            if(b) {
                String command = m.group(1);
                String frequency = m.group(2);
                String removeCommand = generateCronRemoveCommand(command);
                String addCommand = generateCronAddCommand(command, frequency);
                outputCommand += commandHelper.executeShellCommand(removeCommand);
                outputCommand += commandHelper.executeShellCommand(addCommand);
            }

        } else if(inputCommand.contains("cron:add")) {
            // 2) cron:add:command:frequency
            Pattern p = Pattern.compile("cron:add:(.*):(.*)");
            Matcher m = p.matcher(inputCommand);
            boolean b = m.matches();
            if(b) {
                String command = m.group(1);
                String frequency = m.group(2);
                String addCommand = generateCronAddCommand(command, frequency);
                outputCommand += commandHelper.executeShellCommand(addCommand);
            }
        } else if(inputCommand.contains("cron:remove")) {
            Pattern p = Pattern.compile("cron:remove:(.*)");
            Matcher m = p.matcher(inputCommand);
            boolean b = m.matches();
            if(b) {
                String command = m.group(1);
                String removeCommand = generateCronRemoveCommand(command);
                outputCommand += commandHelper.executeShellCommand(removeCommand);
            }
        }
        return outputCommand;
    }

    private String generateCronAddCommand(String command, String frequency) {
        //(crontab -l ; echo '* * * * * /home/pi/CentraleSolaireData/Programmes/majika-riello-monitoring-1.0/bin/majika-riello-monitoring ftp') > crontab.txt && crontab crontab.txt
        String addCommand = "(crontab -l ; echo '" + frequency + " " + thisCommand + " " + command + "') > ~/crontab.txt && crontab ~/crontab.txt";
        return addCommand;
    }

    private String generateCronRemoveCommand(String command) {
        String removePattern = thisCommand + " " + command;
        //crontab -l | grep -v 'PATTERN' >crontab.txt && crontab crontab.txt
        String removeCommand = "crontab -l | grep -v '" + removePattern + "'  >~/crontab.txt && crontab ~/crontab.txt";
        return removeCommand;
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

    public CommandHelper getCommandHelper() {
        return commandHelper;
    }

    public void setCommandHelper(CommandHelper commandHelper) {
        this.commandHelper = commandHelper;
    }

}
