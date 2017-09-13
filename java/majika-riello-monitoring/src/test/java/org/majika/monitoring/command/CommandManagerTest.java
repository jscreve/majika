package org.majika.monitoring.command;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.majika.monitoring.csv.ApplicationCsv;
import org.majika.monitoring.ftp.FtpHelper;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@RunWith(MockitoJUnitRunner.class)
public class CommandManagerTest {

    private static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ApplicationCsv.class);

    @Test
    public void testExecuteCommand() throws IOException {
        FTPClient ftpClient = null;
        try {
            CommandManager commandManager = new CommandManager();

            //connect to FTP
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            Properties properties = new Properties();
            properties.load(input);
            String remoteFolder =  properties.getProperty("commandRemoteFolder");

            //put a remote command remotely, command may depend on System
            String command = "shell:ls";
            String remoteFile = remoteFolder + "TestCommand";
            ftpClient = FtpHelper.connectToFTP(properties);
            FtpHelper.storeRemoteFile(ftpClient, command, remoteFile);

            //test it
            commandManager.executeCommand();

            //retrieve remote file result, need to get a new ftp connection
            String remoteFileResult = remoteFolder + properties.getProperty("commandResultFile");
            ftpClient = FtpHelper.connectToFTP(properties);
            String output = FtpHelper.getRemoteFileContent(ftpClient, remoteFileResult);
            logger.info(output);
            Assert.assertNotNull(output);
        } catch(IOException e) {
            logger.error(e);
            throw e;
        } finally {
            FtpHelper.disconnectFTP(ftpClient);
        }
    }
}
