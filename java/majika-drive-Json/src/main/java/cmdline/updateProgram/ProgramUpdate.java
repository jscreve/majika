package cmdline.updateProgram;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * Created by etienne on 17/08/2017.
 */
public class ProgramUpdate {

    public static void main(String[] args) {
        new ProgramUpdate();

    }

    public ProgramUpdate() {

        Logger logger = LogManager.getLogger();
        String server = "files.000webhost.com";
        String user = "majikamonitoringamp";
        String pass = "majikamada";
        Properties prop = new Properties();
        FTPClient ftpClient = new FTPClient();
        int port = 21;
        Date date = new Date();
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            FTPFile[] ftplist = ftpClient.listFiles("majika/DevelopperFolder/");
            String infoDownloading = "Files ";
            for (FTPFile ftpfile : ftplist) {
                if (ftpfile.isFile() && !ftpfile.getName().equals("Success.txt")) {
                    //System.out.println(ftpfile.getName());
                    String remoteFile1 = "/majika/DevelopperFolder/" + ftpfile.getName();

                    File downloadJar = new File(prop.getProperty("jarPath") + ftpfile.getName());
                    OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadJar));
                    logger.info("Start Downloading File");
                    boolean successJar = ftpClient.retrieveFile(remoteFile1, outputStream1);
                    outputStream1.close();
                    ftpClient.deleteFile(remoteFile1);
                    infoDownloading = infoDownloading + ":" + ftpfile.getName();

                    if (successJar) {
                        logger.info(ftpfile.getName() + " has been downloaded successfully.");
                    }
                }
            }
                if (!infoDownloading.equals("Files ")) {
                    try {
                        FileWriter fw = new FileWriter(prop.getProperty("jarPath") + "Success.txt");
                        BufferedWriter fileDownloaded = new BufferedWriter(fw);
                        fileDownloaded.write(infoDownloading + " has been downloaded on raspberry Pi successfully, date : " + date);
                        fileDownloaded.close();
                        fw.close();

                        File firstLocalFile = new File(prop.getProperty("jarPath") + "Success.txt");

                        String firstRemoteFile = "/majika/DevelopperFolder/Success.txt";
                        InputStream inputStream = new FileInputStream(firstLocalFile);

                        logger.info("Start uploading Dwn file");
                        ftpClient.storeFile(firstRemoteFile, inputStream);
                        inputStream.close();


                    } catch (IOException e) {
                        logger.error("Problem to create Dowloading file", e);

                    }
                }
            } catch(IOException ex){
                logger.error("Connection Internet off", ex);
            } finally{
                try {
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }
                } catch (IOException ex) {
                    logger.error("Disconnection FTP", ex);
                }
        }
    }
}
