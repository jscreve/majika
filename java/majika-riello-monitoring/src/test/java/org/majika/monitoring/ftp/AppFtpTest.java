package org.majika.monitoring.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.majika.monitoring.csv.ApplicationCsv;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.LogManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppFtpTest {

    @Mock
    FileJsonFtp fileJsonFtp;

    @InjectMocks
    private AppFtp appFtp;

    private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(AppFtpTest.class);

    @Test
    public void testExecuteFTPCommand() throws Exception {
        //override ftp files path
        String filePath = Paths.get("ftp.json").toAbsolutePath().normalize().toString();
        String directoryPath = new File(filePath).getParent().toString() + "/src/test/resources/";
        appFtp.setJsonRemoteDirectory("majika/DevelopperFolder/testFtp/");
        appFtp.setJsonFileName("ftp.json");
        appFtp.setPathDirFile(directoryPath);
        appFtp.executeFTPCommand();

        //check ftp remote file
        InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
        Properties prop = new Properties();
        prop.load(input);
        input.close();
        FTPClient ftpClient = FtpHelper.connectToFTP(prop);
        String remoteFileName = "majika/DevelopperFolder/testFtp/ftp.json";
        String output = FtpHelper.getRemoteFileContent(ftpClient, remoteFileName);
        logger.info(output);
        Assert.assertNotNull(output);

        //remove remote file
        FtpHelper.removeRemoteFile(ftpClient, remoteFileName);

    }
}
