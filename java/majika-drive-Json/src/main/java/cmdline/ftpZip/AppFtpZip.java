package cmdline.ftpZip;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by etienne on 09/08/2017.
 */
public class AppFtpZip {


    public static void main(String[] args) {
        new AppFtpZip();

    }

    /**
     * Comme pour le ftpJson, on se connecte au serveur FTP, mais cette fois ci au lieu d'envoyer un fichier Json on envoie un fichier .zip
     */
    public AppFtpZip() {

        Logger logger = LogManager.getLogger();
        String server = "files.000webhost.com";
        String user = "majikamonitoringamp";
        String pass = "majikamada";
        Properties prop = new Properties();
        FTPClient ftpClient = new FTPClient();
        Zip zip = new Zip();
        int port = 21;
        Date date = new Date();
        SimpleDateFormat zipFormat = new SimpleDateFormat("dd_MM_yyyy");
        SimpleDateFormat folderMonthly = new SimpleDateFormat("MM_yyyy");
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);


            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String dirMonthly = folderMonthly.format(date);
            if (!ftpClient.changeWorkingDirectory("/majika/CsvZip/" + dirMonthly)) {
                ftpClient.makeDirectory("/majika/CsvZip/" + dirMonthly);
            }


            // APPROACH #1: uploads first file using an InputStream
            File firstLocalFile = new File(zip.setZipPath());


            String firstRemoteFile = "/majika/CsvZip/" + dirMonthly + "/" + zipFormat.format(date).toString() + ".zip";
            InputStream inputStream = new FileInputStream(firstLocalFile);

            logger.info("Start uploading first file");
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                logger.info("File Uploaded");
            }

        } catch (IOException ex) {
            logger.error("Connection Internet off", ex);
        } finally {
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
