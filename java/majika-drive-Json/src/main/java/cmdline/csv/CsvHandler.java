package cmdline.csv;

/**
 * Created by etienne on 28/07/2017.
 */
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CsvHandler {

    private String[] tabNomAdresseCsv;
    private String[] tabAdresseRegisterCsv;
    private ModbusClient modbusClient;
    private String[] divisePar10;
    private double valeur;

    private static final String csvEnd = ".csv";
    private static final String separateur = "\\";
    private int i = 0;
    private Logger logger = LogManager.getLogger();

    //Fonction définissant les 3variables à la fois, permet de créer pour les 3 onduleurs
    public CsvHandler(ModbusClient modbus, String[] pNomAdd, String[] pAddReg, String[] pDivise10) throws IOException, ModbusException {
        this.modbusClient = modbus;
        this.tabNomAdresseCsv = pNomAdd;
        this.tabAdresseRegisterCsv = pAddReg;
        this.divisePar10 = pDivise10;

    }
    /**
     *
     * @param pathDir Directroy indicated from the config file, where te csv will be created
     * @param name name of the file
     * @throws IOException
     */
    public void setFileCsv(String pathDir, String name) throws IOException {
        //Création du nom du fichier et de son emplacement dans la carte SD
        Date date = new Date();
        SimpleDateFormat month = new SimpleDateFormat("dd_MM_yyyy");
        String dir = setDirMonthly(pathDir, date);
        //Path pour créer un dossier chaque jour(utile pour zipper)
        String dirDaily = setDirdaily(dir, date, month);
        //Nom et Path pour créer le fichier
        String allPath = dirDaily + separateur + name + month.format(date).toString() + csvEnd;
        //Heure et jour dans le fichier .Csv
        SimpleDateFormat day = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
        try (FileWriter fw = new FileWriter(allPath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);
             FileReader fr = new FileReader(allPath)) {
            //Ecrit l'entete seulement si la lettre D (de date) n'est pas déjà écrite
            if ((fr.read() != 68)) {
                for (int i = 0; i < tabNomAdresseCsv.length; i++) {
                    out.print(tabNomAdresseCsv[i] + ";");
                }
            }
            out.println();  //Saute de ligne
            out.print(day.format(date).toString() + ";"); //Ecrit la date du jour
            out.print(hour.format(date).toString() + ";"); //Ecrit l'heure

            //######################################
            modbusClient.Connect();
            //######################################

            for (i = 0; i < tabAdresseRegisterCsv.length; i++) {
                //Fonction de recuperation des données arduino
                //i = arduino(name, out, i); //££££££££££££££££££££££££££££££
                //Fin de la fonction, il manque dans le nom des adresses du fichier csv, le nom des variables arduino !
                valeur = modbusClient.ReadHoldingRegisters(Integer.parseInt(tabAdresseRegisterCsv[i]), 1)[0];
                //Permet de diviser la valeur par 10 pour obtenir la valeur réelle et lisible
                for (String add : divisePar10) {
                    if (tabAdresseRegisterCsv[i].equals(add)) {
                        valeur = valeur / 10;
                    }
                }
                out.print(valeur + ";");
            }
            //####################################
            modbusClient.Disconnect();
            //####################################
            //Ferme toutes les ouvertures pour le prochain fichier
            out.close();
            fr.close();
            bw.close();
            fw.close();
        } catch (IOException e) {
            logger.error("Error: " + e);
        } catch (ModbusException m) {
            logger.error(m);
        } catch (Exception e) {
            logger.error(e);
        }
    }
    private double getValueTempAndElMeter(String command) {
        Double returnedValue = null;
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            returnedValue = new Double(in.readLine()).doubleValue();
            System.out.println("value is : " + returnedValue);
            in.close();
        } catch (Exception e) {
            System.err.println("Exception occured reading temperature");
            logger.error("Arduino Error : " + e);
        }

        return returnedValue;

    }




    private int arduino(String name, PrintWriter out, int i) {
        ////////////////Recuperation des donnée de la carte Arduino dans le .csv excel
        String returnedValue = "null";
        String[] array;
        if (name.equals("UPS_SPS_Battery_")) {
            while (i < 3) {
                if (i == 2) {
                    try {
                        Process p = Runtime.getRuntime().exec("python3 fetchTemp.py 3");
                        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        returnedValue = in.readLine();
                        array = returnedValue.split(",");
                        for (String compteur : array) {
                            out.print(Integer.parseInt(compteur) + ";");
                            System.out.println("value is : " + Integer.parseInt(compteur));
                        }
                        in.close();
                    } catch (Exception e) {
                        System.err.println("Exception occured reading temperature");
                        logger.error("Arduino Error" + e);
                    }
                } else {
                    out.print(getValueTempAndElMeter("python3 fetchTemp.py " + i + 1) + ";");
                    //System.out.println("python3 fetchTemp.py " + (i + 1));
                    //out.print(i + 1 + ";");
                }
                i++;
            }
        }
        //Fin de récuperation des donnée
        return i;
    }

    private String setDirdaily(String dir, Date date, SimpleDateFormat month) {

        String dirDaily = dir + separateur + month.format(date).toString();
        File theDirDaily = new File(dirDaily);
        if (!theDirDaily.exists()) {
            try {
                theDirDaily.mkdir();
            } catch (SecurityException se) {
                logger.error("ErrorDirDaily :" + se);
            }
        }
        return dirDaily;
    }

    private String setDirMonthly(String pathDir, Date date) {

        SimpleDateFormat df = new SimpleDateFormat("MM_yyyy");
        String dir = pathDir + df.format(date).toString();
        File theDir = new File(dir);
        if (!theDir.exists()) {
            try {
                theDir.mkdir();
            } catch (SecurityException de) {
                logger.error("ErrorDirMonth : " + de);
            }
        }
        return dir;

    }

}