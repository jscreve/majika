package org.majika.monitoring.shell;

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
public class ShellManagerTest {

    private static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ApplicationCsv.class);

    @Test
    public void testExecuteShellCommand() throws IOException {
        FTPClient ftpClient = null;
        try {
            ShellManager shellManager = new ShellManager();

            //connect to FTP
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            Properties properties = new Properties();
            properties.load(input);

            //put a remote command remotely, command may depend on System
            String command = "ls";
            String remoteFolder = "majika/DevelopperFolder/testShell/";
            String remoteFile = remoteFolder + "TestCommand";
            shellManager.setShellRemoteFolder(remoteFolder);
            ftpClient = FtpHelper.connectToFTP(properties);
            FtpHelper.storeRemoteFile(ftpClient, command, remoteFile);

            //test it
            shellManager.executeShellCommand();

            //retrieve remote file result, need to get a new ftp connection
            String remoteFileResult = remoteFolder + properties.getProperty("shellResultFile");
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
