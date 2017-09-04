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

    private static final String csvEnd = ".csv";
    private static final String separateur = "/";
    private double valeur;
    private int i = 0;
    private Logger logger = LogManager.getLogger();
    Properties prop = new Properties();

    public CsvHandler() {}

    /**
     * @param pathDir Directroy indicated from the config file, where te csv will be created
     * @throws IOException
     */
    public void setFileCsv(String pathDir, String pathArduino, String[] tabNomAdresseSPS, String[] tabNomAdresseSol, ModbusClient modbusClientSPS, ModbusClient modbusClientEast, ModbusClient modbusClientWest, String[] tabAdresseRegisterSPS, String[] tabAdresseRegisterSol, String[] tabAd10SPS, String[] tabAd10Sol) {
        //Création du nom du fichier et de son emplacement dans la carte SD

        Date date = new Date();
        SimpleDateFormat month = new SimpleDateFormat("dd_MM_yyyy");
        String dir = setDirMonthly(pathDir, date);
        //Path pour créer un dossier chaque jour(utile pour zipper)
        String dirDaily = setDirdaily(dir, date, month);
        //Nom et Path pour créer le fichier
        String allPath = dirDaily + separateur + "UPS_" + month.format(date) + csvEnd;
        //Heure et jour dans le fichier .Csv
        SimpleDateFormat day = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
        try (FileWriter fw = new FileWriter(allPath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);
             FileReader fr = new FileReader(allPath)) {

            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);


            //Ecrit l'entete seulement si la lettre D (de date) n'est pas déjà écrite
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
            out.print(day.format(date) + ";"); //Ecrit la date du jour
            out.print(hour.format(date) + ";");


            //######################################

            if (tabNomAdresseSPS[0].equals("Date")) {
                try {
                    modbusClientSPS.Connect();

                    //FileWriter arduinoWF = new FileWriter("/home/pi/CentraleSolaireData/Programmes/majika-drive-sample-1.0/bin/arduino");
                    //BufferedWriter arduinoWB = new BufferedWriter(arduinoWF);

                 try{
                    /*Process p = Runtime.getRuntime().exec(pathArduino);
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String returnedValue = in.readLine();*/
                    String returnedValue = "299999,28.8,29.9,292,191,292,8888";
                    if(returnedValue.equals("Error in socket connection"))
                    {
                        for(int i =0; i<7;i++){
                            out.print(0 +";");
                            //arduinoWB.write("0,");
                        }

                    }else {
                        String[] array = returnedValue.split(",");
                        String pointToComma = "";
                        //arduinoWB.write(returnedValue);
                        for (String compteur : array) {
                            pointToComma = compteur.replace(".",",");
                            out.print(pointToComma + ";");
                        }
                          //          arduinoWB.close();
                            //        arduinoWF.close();

                    }}catch(Exception e){
                    logger.error("Arduino Bluetooth",e);
                }
                    writeToCsv(modbusClientSPS, tabAdresseRegisterSPS, tabAd10SPS, out);
                    modbusClientSPS.Disconnect();
                } catch (Exception e) {
                    logger.error("Impossible to Connect to SPS", e);
                }
            }
            try {
                modbusClientEast.Connect();
                writeToCsv(modbusClientEast, tabAdresseRegisterSol, tabAd10Sol, out);
                modbusClientEast.Disconnect();
            } catch (Exception m) {
                logger.error("Impossible to connect to East", m);
            }

            try {
                modbusClientWest.Connect();
                writeToCsv(modbusClientWest, tabAdresseRegisterSol, tabAd10Sol, out);
                modbusClientWest.Disconnect();
            } catch (Exception m) {
                logger.error("Impossible to connect to West", m);
            }

            out.close();
            fr.close();
            bw.close();
            fw.close();
            input.close();
        } catch (IOException e) {
            logger.error("Error: ", e);
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }


    private void writeToCsv(ModbusClient modbusClient, String[] tabAdresseRegister, String[] tabAd10, PrintWriter out) {
        try {
            for (i = 0; i < tabAdresseRegister.length; i++) {

                valeur = modbusClient.ReadHoldingRegisters(Integer.parseInt(tabAdresseRegister[i]), 1)[0];
                //Permet de diviser la valeur par 10 pour obtenir la valeur réelle et lisible
                for (String add : tabAd10) {
                    if (tabAdresseRegister[i].equals(add)) {
                        valeur = valeur / 10;
                        if (tabAdresseRegister[i].equals("23")) {
                            valeur = valeur / 10;
                        }
                    }
                }
                //Remplace les points du float par une virgule pour que ce soit directement utilisable par le .csv
                String valString = String.valueOf(valeur).replace(".",",");
                //le ";" permet de sauter d'une colonne en excel
                out.print(valString + ";");

            }
        } catch (Exception e) {logger.error("Impossible to get value from",e);
        }
    }

    private String setDirdaily(String dir, Date date, SimpleDateFormat month) {

        String dirDaily = dir + separateur + month.format(date);
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

    /**
     *
     * @param pathDir Adresse de création du dossier chaque mois
     * @param date Utilisation de la même date au lancement du programme
     * @return retourne l'adresse du dossier avec le mois
     */
    private String setDirMonthly(String pathDir, Date date) {

        SimpleDateFormat df = new SimpleDateFormat("MM_yyyy");
        String dir = pathDir + df.format(date);
        File theDirMonthly = new File(dir);
        if (!theDirMonthly.exists()) {
            try {
                theDirMonthly.mkdir();
            } catch (SecurityException de) {
                logger.error("ErrorDirMonth : " + de);
            }
        }
        return dir;

    }

}