package org.majika.monitoring.ftpZip;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.majika.monitoring.ftp.AppFtp;
import org.majika.monitoring.ftp.FileJsonFtp;
import org.majika.monitoring.ftp.FtpHelper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppFtpZipTest {

    private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(AppFtpZipTest.class);

    @Mock
    private Zip zip;

    @InjectMocks
    private AppFtpZip appFtpZip;

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Before
    public void init() throws IOException {

    }

    @Test
    public void testSuccessExecuteFtpZipCommand() throws IOException {
        String pathToNewFile = folder.newFile().getPath();
        when(zip.getOrCreateZipFile()).thenReturn(pathToNewFile);
        appFtpZip.executeFtpZipCommand();

        //check remote ftp
        InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
        Properties prop = new Properties();
        prop.load(input);
        input.close();
        FTPClient ftpClient = FtpHelper.connectToFTP(prop);
        String output = FtpHelper.getRemoteFileContent(ftpClient, appFtpZip.getRemoteFileName());
        logger.info(output);
        Assert.assertNotNull(output);

        //remove file
        FtpHelper.removeRemoteFile(ftpClient, appFtpZip.getRemoteFileName());
    }

    //TODO test retry

}
