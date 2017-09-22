package org.majika.monitoring.command;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.majika.monitoring.csv.ApplicationCsv;
import org.majika.monitoring.ftp.FtpHelper;
import org.majika.monitoring.ftpZip.AppFtpZip;
import org.majika.monitoring.util.CommandHelper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CommandManagerTest {

    private static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ApplicationCsv.class);

    @Mock
    private AppFtpZip appFtpZip;

    @Spy
    private CommandHelper commandHelper;

    @InjectMocks
    private CommandManager commandManager;

    //full tests
    @Test
    public void testExecuteShellCommand() throws IOException {
        FTPClient ftpClient = null;
        try {
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
            commandManager.executeCommandFromFTP();

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

    @Test
    public void testExecuteFtpZipCommand() throws IOException {
        FTPClient ftpClient = null;
        try {
            //connect to FTP
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            Properties properties = new Properties();
            properties.load(input);
            String remoteFolder =  properties.getProperty("commandRemoteFolder");

            //put a remote command remotely, command may depend on System
            String command = "ftpzip:14_09_2017";
            String remoteFile = remoteFolder + "TestCommand";
            ftpClient = FtpHelper.connectToFTP(properties);
            FtpHelper.storeRemoteFile(ftpClient, command, remoteFile);

            //test it
            commandManager.executeCommandFromFTP();

            //retrieve remote file result, need to get a new ftp connection
            String remoteFileResult = remoteFolder + properties.getProperty("commandResultFile");
            ftpClient = FtpHelper.connectToFTP(properties);
            String output = FtpHelper.getRemoteFileContent(ftpClient, remoteFileResult);
            logger.info(output);
            Assert.assertNotNull(output);
            verify(appFtpZip, times(1)).executeFtpZipCommand();
        } catch(IOException e) {
            logger.error(e);
            throw e;
        } finally {
            FtpHelper.disconnectFTP(ftpClient);
        }
    }

    //unit tests
    @Test
    public void testExecuteAddCronCommand() throws IOException {
        //put a remote command remotely, command may depend on System
        String command = "cron:add:toto:*/2 * * * *";
        commandManager.internalExecuteCommand(command);
        verify(commandHelper).executeShellCommand("(crontab -l ; echo '*/2 * * * * /home/pi/CentraleSolaireData/Programmes/majika-riello-monitoring-1.0/bin/majika-riello-monitoring toto') > ~/crontab.txt && crontab ~/crontab.txt");
    }

    @Test
    public void testExecuteRemoveCronCommand() throws IOException {
        //put a remote command remotely, command may depend on System
        String command = "cron:remove:toto";
        commandManager.internalExecuteCommand(command);
        verify(commandHelper).executeShellCommand("crontab -l | grep -v '/home/pi/CentraleSolaireData/Programmes/majika-riello-monitoring-1.0/bin/majika-riello-monitoring toto'  >~/crontab.txt && crontab ~/crontab.txt");
    }

    @Test
    public void testExecuteUpdateCronCommand() throws IOException {
        //put a remote command remotely, command may depend on System
        String command = "cron:update:toto:*/2 * * * *";
        commandManager.internalExecuteCommand(command);
        verify(commandHelper).executeShellCommand("crontab -l | grep -v '/home/pi/CentraleSolaireData/Programmes/majika-riello-monitoring-1.0/bin/majika-riello-monitoring toto'  >~/crontab.txt && crontab ~/crontab.txt");
        verify(commandHelper).executeShellCommand("(crontab -l ; echo '*/2 * * * * /home/pi/CentraleSolaireData/Programmes/majika-riello-monitoring-1.0/bin/majika-riello-monitoring toto') > ~/crontab.txt && crontab ~/crontab.txt");
    }

    @Test
    public void testExecuteGetLogsCommand() throws IOException {
        //put a remote command remotely, command may depend on System
        String command = "getlog";
        commandManager.internalExecuteCommand(command);
        verify(commandHelper).executeCommand("/home/pi/CentraleSolaireData/Programmes/majika-riello-monitoring-1.0/bin/majika-riello-monitoring uploadLogs");
    }
}
