package cmdline.ftp;

import java.io.*;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by etienne on 01/08/2017.
 */
public class AppFtp {


    public static void main(String[] args) {
        new AppFtp();

    }

    /**
     *
     */
    public AppFtp()  {

        Logger logger = LogManager.getLogger();
        String server = "files.000webhost.com";
        String user = "majikamonitoringamp";
        String pass = "majikamada";
        Properties prop = new Properties();
        FTPClient ftpClient = new FTPClient();
        int port = 21;
        try{Thread.sleep(5000);}catch(Exception e){logger.error("Couldnt wait",e);}
        FileJsonFtp fJs = new FileJsonFtp();
        fJs.setFileJson();
            try {
                InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
                prop.load(input);
                ftpClient.connect(server, port);

                ftpClient.login(user, pass);
                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                // APPROACH #1: uploads first file using an InputStream
                File firstLocalFile = new File(prop.getProperty("pathDirFile") + "ups.json");

                String firstRemoteFile = "/public_html/ups.json";
                InputStream inputStream = new FileInputStream(firstLocalFile);

                logger.info("Start uploading Json file");
                boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
                inputStream.close();
                if (done) {
                    logger.info("File Uploaded");
                }
            } catch (IOException ex) {
                logger.error("Connection Internet off", ex);
                //Unless it works otherwise
                try{Runtime.getRuntime().exec("sudo /usr/bin/modem3g/sakis3g connect OTHER=\"USBMODEM\" USBMODEM=\"12d1:1506\" USBINTERFACE=\"0\"");}catch(IOException h){logger.error("USB Modem bugging",h);}
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
