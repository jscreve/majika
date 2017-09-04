package cmdline;

import cmdline.csv.ApplicationCsv;
import cmdline.ftpJson.AppFtp;
import cmdline.ftpZip.AppFtpZip;
import cmdline.updateProgram.ProgramUpdate;
import cmdline.zip.DriveSampleZip;

import java.io.IOException;

/**
 * Created by etienne on 28/07/2017.
 */
public class Application {
    /**
     *
     * @param args les arguments suivants permettent de choisir un des packages du programme. Cela rend l'utilisation de crontab flexible.
     */
    public static void main(String[] args) {
        try {
            switch (args[0]) {
                case "csv": //Enregistrement des données toutes les minutes sur un fichier .csv
                    new ApplicationCsv();
                    break;
                case "ftp": //Envoie du fichier Json pour le site sur un serveur ftpJson
                    new AppFtp();
                    break;
                case "zip": // Envoie du zip sur le drive
                    new DriveSampleZip();
                    break;
                case "ftpZip": // Envoie du zip sur le serveur ftpJson
                    new AppFtpZip();
                    break;
                case "updateProgram":// Regarder dans le dossier DevelopperFolder sur le server ftpJson et telecharge les fichier pour les transférer au raspberry PI
                    new ProgramUpdate();
                    break;
                default:
                    System.out.println("This is not a proper arguments");
                    break;
            }
        } catch (IOException i) {
            System.out.println(i);
        }
    }
}
