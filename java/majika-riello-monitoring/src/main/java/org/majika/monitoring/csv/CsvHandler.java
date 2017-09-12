package org.majika.monitoring.csv;

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
import java.util.Properties;

public class CsvHandler {

    //Private static final est pour les variables qui ne changeront jamais, comme les String ci dessous
    private static final String csvEnd = ".csv";
    private static final String separateur = "/";

    private double valeur;
    private int i = 0;
    private Logger logger = LogManager.getLogger(CsvHandler.class);
    private String arduinoNomVariable;
    private String arduinoDir;

    public CsvHandler() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            Properties prop = new Properties();
            prop.load(input);
            input.close();
            arduinoNomVariable = prop.getProperty("arduinoNomVariable");
            arduinoDir = prop.getProperty("arduinoDir");
        } catch (Exception e) {
            logger.error("Could not load properties file", e);
        }
    }

    /**
     * @param pathDir Directroy indicated from the config file, where te csv will be created
     * @throws IOException
     */
    public void setFileCsv(String pathDir, String pathArduino, String[] tabNomAdresseSPS, String[] tabNomAdresseSol, ModbusClient modbusClientSPS, ModbusClient modbusClientEast, ModbusClient modbusClientWest, String[] tabAdresseRegisterSPS, String[] tabAdresseRegisterSol, String[] tabAd10SPS, String[] tabAd10Sol) {
        //Création du nom du fichier et de son emplacement dans la carte SD
        Date date = new Date();
        //Format du nom de dossier du jour
        SimpleDateFormat month = new SimpleDateFormat("dd_MM_yyyy");
        //Methode qui crée le dossier chaque mois
        String dir = setDirMonthly(pathDir, date);
        //Path pour créer un dossier chaque jour(utile pour zipper)
        String dirDaily = setDirdaily(dir, date, month);
        //Nom et chemin pour créer le fichier .csv
        String allPath = dirDaily + separateur + "UPS_" + month.format(date) + csvEnd;
        //Heure et jour dans le fichier .Csv
        SimpleDateFormat day = new SimpleDateFormat("dd:MM:yyyy");
        SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
        try (FileWriter fw = new FileWriter(allPath, true); // Append permet de ne pas "overwrite" dans le fichier.csv et d'écrire à la dernière ligne
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);
             FileReader fr = new FileReader(allPath)) {

            //Ecrit l'entete seulement si la lettre D (de date) (68 correspondant à la lettre D majuscule) n'est pas déjà écrite
            if ((fr.read() != 68)) {

                for (int i = 0; i < tabNomAdresseSPS.length; i++) {
                    out.print(tabNomAdresseSPS[i] + ";");
                }
                for (int i = 0; i < tabNomAdresseSol.length; i++) {
                    out.print(tabNomAdresseSol[i] + " E;");
                }
                for (int i = 0; i < tabNomAdresseSol.length; i++) {
                    out.print(tabNomAdresseSol[i] + " W;");
                }
            }
            out.println();  //Saute de ligne
            out.print(day.format(date) + ";"); //Ecrit la date du jour, le point virgule permet de passer à une autre colonne dans le format excel
            out.print(hour.format(date) + ";");
            //######################################
            //"Date" ne se trouve que dans le config.properties du SPS et permet donc de reconnaitre le SPS
            if (tabNomAdresseSPS[0].equals("Date")) {

                try {
                    modbusClientSPS.Connect();

                    //Ecriture dans un fichier arduino, qui sera lu par le programme Json
                    FileWriter arduinoWF = new FileWriter(arduinoDir);
                    BufferedWriter arduinoWB = new BufferedWriter(arduinoWF);

                    try {

                        Process p = Runtime.getRuntime().exec(pathArduino);// execution d'une ligne de commande qui se trouve dans le config.properties
                        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));//lecture du renvoie
                        String returnedValue = in.readLine();//Transfert dans une variable toutes la chaîne de caractère
                        //String returnedValue = "29999,28.9,29.8,29.1,27.1,101,102,103,9999";
                        if (returnedValue.equals("Error in socket connection"))// Dans le cas ou la connexion à été interrompu toutes les valeurs sont initialisées à 0
                        {
                            String[] nbrVariableArduino = arduinoNomVariable.split(",");
                            for (int i = 0; i < nbrVariableArduino.length; i++) {//Le fait de prendre les nom de variables permet de determiner à l'avance grâce au config properties le nombre de données reçue
                                out.print(0 + ";");//Ecriture dans le csv
                                arduinoWB.write("0,");//Ecriture dans le fichier
                            }
                        } else {//Si il n'y a pas eu de probleme de communication
                            String[] array = returnedValue.split(",");//Convertit la chaîne de caractere en tableau avec comme séparateur le ","
                            String pointToComma = ""; //Initialisation de la variable
                            arduinoWB.write(returnedValue);// Ecriture dans le fichier pour l'arduino tel quel
                            for (String compteur : array) {// decomposition de chaque donnée
                                pointToComma = compteur.replace(".", ",");// remplacement des . par des ,
                                out.print(pointToComma + ";");//On ecrit dans le csv les valeurs retournées
                            }
                            arduinoWB.close();//On oublie pas de fermer le fichier dans lequel on a écrit pour laisser place à la lecture
                            arduinoWF.close();
                        }
                    } catch (Exception e) {
                        logger.error("Arduino Bluetooth", e);// A ne pas oublier si il y a des erreurs
                    }
                    writeToCsv(modbusClientSPS, tabAdresseRegisterSPS, tabAd10SPS, out);//Methode pour lire les données du SPS
                    modbusClientSPS.Disconnect();//Deconnexion du SPS

                } catch (Exception e) {
                    logger.error("Impossible to Connect to SPS", e);
                }
            }
            try {
                modbusClientEast.Connect();//Même chose sans la partie Arduino pour l'onduleur Solaire Est
                writeToCsv(modbusClientEast, tabAdresseRegisterSol, tabAd10Sol, out);
                modbusClientEast.Disconnect();
            } catch (Exception m) {
                logger.error("Impossible to connect to East", m);
            }

            try {
                modbusClientWest.Connect();//Puis Ouest
                writeToCsv(modbusClientWest, tabAdresseRegisterSol, tabAd10Sol, out);
                modbusClientWest.Disconnect();
            } catch (Exception m) {
                logger.error("Impossible to connect to West", m);
            }
            out.close();// Ne pas oublier de fermet toutes les ouvertures réalisées en debut de methode
            fr.close();
            bw.close();
            fw.close();
        } catch (IOException e) {
            logger.error("IOError", e);
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }


    private void writeToCsv(ModbusClient modbusClient, String[] tabAdresseRegister, String[] tabAd10, PrintWriter out) {
        try {
            for (i = 0; i < tabAdresseRegister.length; i++) {// lit jusqu'a la derniere valeur du tableau
                //
                valeur = modbusClient.ReadHoldingRegisters(Integer.parseInt(tabAdresseRegister[i]), 1)[0];//Integer.parseInt est primordial car il permet de convertir un String en un Int, la methode Holding register n'accepte que des int
                //Permet de diviser la valeur par 10 de celles qui doivent être divisé pour obtenir la valeur réelle et lisible
                for (String add : tabAd10) {
                    if (tabAdresseRegister[i].equals(add)) {
                        valeur = valeur / 10;
                        if (tabAdresseRegister[i].equals("23") && tabAdresseRegister[0].equals("10")) {// Il n'y a qu'une valeur dans les solaires (la fréquence) qui doit etre divisé par 100
                            //Attention à modifier cette methode de division si l'adresse 23 est utilisé dans l'SPS
                            valeur = valeur / 10;
                        }
                    }
                }
                //Cette condition if check si c'est bien un onduleur d'ou la comparaison avec 10 qui est la premiere valeur lu des onduleurs et si c'est bien 24 25 ou 26
                if (tabAdresseRegister[0].equals("10") && (tabAdresseRegister[i].equals("24") || tabAdresseRegister[i].equals("25") || tabAdresseRegister[i].equals("26"))) {
                    valeur = valeur * 10;
                }
                //Remplace les points du float par une virgule pour que ce soit directement utilisable par le .csv
                String valString = String.valueOf(valeur).replace(".", ",");
                //le ";" permet de sauter d'une colonne en excel
                out.print(valString + ";");
            }
        } catch (Exception e) {
            logger.error("Impossible to get value from", e);
        }
    }

    /**
     * @param dir   Premier chemin indiquant là ou seront les Csv
     * @param date  La meme date pour tout le programme
     * @param Daily Le format de date du jour pour le nom du dossier et le futur nom du fichier.csv
     * @return
     */
    private String setDirdaily(String dir, Date date, SimpleDateFormat Daily) {
        //Creatin du chemin pour le dossier Quotidien
        String dirDaily = dir + separateur + Daily.format(date);
        File theDirDaily = new File(dirDaily);
        if (!theDirDaily.exists()) {
            try {
                boolean done = theDirDaily.mkdir();// On crée le dossier du jour
                if (done) {
                    logger.info("Folder created the :" + Daily.format(date));
                }
            } catch (SecurityException se) {
                logger.error("ErrorMkDirDaily", se);
            }
        }
        return dirDaily;// On retourne le chemin qui servira à la création du chemin pour le .csv
    }

    /**
     * @param pathDir Adresse de création du dossier chaque mois
     * @param date    Utilisation de la même date au lancement du programme
     * @return retourne l'adresse du dossier avec le mois
     */
    private String setDirMonthly(String pathDir, Date date) {
        SimpleDateFormat df = new SimpleDateFormat("MM_yyyy");
        String dir = pathDir + df.format(date);
        File theDirMonthly = new File(dir);
        if (!theDirMonthly.exists()) {
            try {
                boolean done = theDirMonthly.mkdir();
                if (done) {
                    logger.info("Monthly folder created the :" + df.format(date));
                }
            } catch (SecurityException de) {
                logger.error("ErrorDirMonth", de);
            }
        }
        return dir;
    }
}