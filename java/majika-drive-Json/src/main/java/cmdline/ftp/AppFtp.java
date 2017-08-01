package cmdline.ftp;

import java.io.*;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 * Created by etienne on 01/08/2017.
 */
public class AppFtp {
    private Properties prop = new Properties();
    private FTPClient ftpClient = new FTPClient();
    private FileInputStream fis = null;
    private FileJsonFtp path = new FileJsonFtp();
    private String server = "files.000webhost.com";
    int port = 21;
    private String user = "majikamonitoringamp";
    private String pass = "majikamada";


    public static void main(String[] args) {
        AppFtp application = new AppFtp();

    }

    public AppFtp() {

        FileJsonFtp fJs = new FileJsonFtp();
        fJs.setFileJson();
        try {FileInputStream input = new FileInputStream("config.properties");
            prop.load(input);

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // APPROACH #1: uploads first file using an InputStream
            File firstLocalFile = new File(prop.getProperty("pathDirFile") + "ups.json");

            String firstRemoteFile = "/public_html/ups.json";
            InputStream inputStream = new FileInputStream(firstLocalFile);

            System.out.println("Start uploading first file");
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                System.out.println("The first file is uploaded successfully.");
            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
