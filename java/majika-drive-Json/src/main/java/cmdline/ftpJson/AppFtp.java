package cmdline.ftpJson;

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
        //
        Logger logger = LogManager.getLogger();
        Properties prop = new Properties();
        //Initialisation des variables de connexions au serveur ftp
        String server = "files.000webhost.com";
        String user = "majikamonitoringamp";
        String pass = "majikamada";

        FTPClient ftpClient = new FTPClient();
        int port = 21;
        //Attend 5secondes pour que le package Csv ait finit d'écrire dans le Arduino et donc laisse libre à la lecture
        try{Thread.sleep(5000);}catch(Exception e){logger.error("Couldnt wait",e);}
        //Initialisation de l'objet de la classe FileJsonFtp
        FileJsonFtp fJs = new FileJsonFtp();
        fJs.setFileJson();//Utilisation de la methode pour créer le fichier .json avec toutes les données actualisées
            try {
                InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
                prop.load(input);//charge le fichier properties
                ftpClient.connect(server, port);// se connecte au serveur
                ftpClient.login(user, pass); // S'enregistre
                ftpClient.enterLocalPassiveMode(); //Passive mode is commonly used for avoid connection problem
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);//How will be the transfer

                // APPROACH #1: uploads first file using an InputStream
                File firstLocalFile = new File(prop.getProperty("pathDirFile") + "ups.json");//Fichier qui va être upload

                String firstRemoteFile = "/public_html/ups.json";//Emplacement où il va etre upload sur le server
                InputStream inputStream = new FileInputStream(firstLocalFile); //Commence la connection

                logger.info("Start uploading Json file");
                boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);//Methode qui fait l'upload
                inputStream.close();//ferme la connection
                if (done) {
                    logger.info("File Uploaded");
                }
            } catch (IOException ex) {
                logger.error("Connection Internet off", ex);
                //Unless it works otherwise
                //try{Runtime.getRuntime().exec("sudo /usr/bin/modem3g/sakis3g connect OTHER=\"USBMODEM\" USBMODEM=\"12d1:1506\" USBINTERFACE=\"0\"");}catch(IOException h){logger.error("USB Modem bugging",h);}
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
