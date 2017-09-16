package org.majika.monitoring.updateProgram;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.majika.monitoring.ftp.FtpHelper;
import org.majika.monitoring.ftpZip.AppFtpZip;
import org.majika.monitoring.ftpZip.Zip;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProgramUpdateTest {

    private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(ProgramUpdateTest.class);

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Before
    public void init() throws IOException {

    }

    @Test
    public void testSuccessProgramUpdateCommand() throws IOException {
        //temp folder to store updates
        File tempFolder = folder.newFolder();

        InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
        Properties prop = new Properties();
        prop.load(input);
        input.close();
        FTPClient ftpClient = FtpHelper.connectToFTP(prop);

        //put a remote config dir with a file into it
        String remoteDir = prop.getProperty("programUpdateRemoteFolder") + "config/";
        String remoteConfigFile = remoteDir + "config.properties";
        String remoteLogFile = prop.getProperty("programUpdateRemoteFolder") + "log4j2.xml";
        ftpClient.makeDirectory(remoteDir);
        //put config.properties into it
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.properties");
        ftpClient.storeFile(remoteConfigFile, inputStream);
        //put log4j.xml at root
        inputStream = this.getClass().getClassLoader().getResourceAsStream("log4j2.xml");
        ftpClient.storeFile(remoteLogFile, inputStream);

        //initiate an update
        ProgramUpdate programUpdate = new ProgramUpdate();
        programUpdate.setJarPath(tempFolder.getPath() + "/");
        programUpdate.executeUpdateCommand();

        //check local folder
        List<String> filesNames = new ArrayList<String>();
        getFilesNames(tempFolder, filesNames);
        logger.info(filesNames);
        Assert.assertTrue(filesNames.contains("config.properties"));
        Assert.assertTrue(filesNames.contains("log4j2.xml"));

        //remote remote files
        boolean success = FtpHelper.removeRemoteFile(ftpClient, remoteConfigFile);
        logger.info("deletion succeeded : " + success);
        success = FtpHelper.removeRemoteFile(ftpClient, remoteLogFile);
        logger.info("deletion succeeded : " + success);

        FtpHelper.disconnectFTP(ftpClient);
    }

    private void getFilesNames(File folder, List<String> filesNames) {
        File[] files = folder.listFiles();
        for(File file : files) {
            if(file.isFile()) {
                filesNames.add(file.getName());
            }
            if(file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
                getFilesNames(file, filesNames);
            }
        }
    }
}
