package cmdline.ftpJson;


import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class FileJsonFtp {

    private float valeur;

    private JSONObject obj = new JSONObject(); // Objet principal du format Json
    private JSONArray listSPS = new JSONArray(); //Liste qui contient les valeurs
    private JSONArray listSolW = new JSONArray(); //Liste qui contient les valeurs
    private JSONArray listSolE = new JSONArray(); //Liste qui contient les valeurs
    private JSONArray list = new JSONArray(); // date
    private JSONObject dataSPS = new JSONObject();//Objet qui formatte
    private JSONObject dataEast = new JSONObject();//Objet qui formatte
    private JSONObject dataWest = new JSONObject();//Objet qui formatte
    public String pathDir;
    private Properties prop = new Properties();
    private Logger logger = LogManager.getLogger();
    private String[] tabAdArduino;

    /**
     * This is the main method called by the main class to create and get data in the json file
     */

    public void setFileJson() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input); // open the connection to the properties file
            //Convertit chaque chaîne de charactere en un tableau séparé par les virgules
            String[] tabAdSPS = prop.getProperty("tabAdresseSPSJson").split(",");
            String[] tabAdSol = prop.getProperty("tabAdresseSolJson").split(",");
            String[] tabAd10SPS = prop.getProperty("divisePar10SPS").split(",");
            String[] tabAd10Sol = prop.getProperty("divisePar10Sol").split(",");
            String[] tabAdNameSPS = prop.getProperty("tabNomVariableSPSJson").split(",");
            String[] tabAdNameSol = prop.getProperty("tabNomVariableSolJson").split(",");

            ModbusClient modbusClientSolEast = new ModbusClient(prop.getProperty("ipAdresseSolEast"), 502);
            ModbusClient modbusClientSolWest = new ModbusClient(prop.getProperty("ipAdresseSolWest"), 502);
            ModbusClient modbusClientSPS = new ModbusClient(prop.getProperty("ipAdresseSPS"), 502);
            //Date au lancement du programme
            Date date = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("HH:mm dd.MM.yyyy");
            //Liste pour la date du fichier Json
            list.add(ft.format(date));

            try {
                modbusClientSPS.Connect();
                //Même technique que pour le csv
                dataUPS(prop.getProperty("nameSPS"), modbusClientSPS, tabAdNameSPS, tabAdSPS, tabAd10SPS, dataSPS, listSPS);
                modbusClientSPS.Disconnect();
            } catch (Exception e) {//Dans le cas où les données n'ont pas pu être récupéré, on revoie la valeur 0 sur chacune des valeurs
                for (int i = 0; i < tabAdNameSPS.length; i++) {
                    dataSPS.put(tabAdNameSPS[i], 0);
                }
                //l'onduleur SPS affiche en plus des solaire le OUTSPS avec la moyenne effectuée en Amont
                dataSPS.put("OUTSPS", 0);
                //Enregistre les données dans la liste
                listSPS.add(dataSPS);
                //L'inscrit dans l'objet principale avec le nom du sps
                obj.put(prop.getProperty("nameSPS"), listSPS);
                logger.error("SPS", e);
            }

            try {
                modbusClientSolEast.Connect();
                dataUPS(prop.getProperty("nameSolEast"), modbusClientSolEast, tabAdNameSol, tabAdSol, tabAd10Sol, dataEast, listSolE);
                modbusClientSolEast.Disconnect();
            } catch (Exception e) {//Même fonctionnement que pour le SPS à part que c'est Power à la place de OUTSPS
                for (int i = 0; i < tabAdNameSol.length; i++) {
                    dataEast.put(tabAdNameSol[i], 0);
                }
                dataEast.put("Power", 0);
                listSolE.add(dataEast);
                obj.put(prop.getProperty("nameSolEast"), listSolE);
                logger.error("SolEast",e);
            }

            try {
                modbusClientSolWest.Connect();
                dataUPS(prop.getProperty("nameSolWest"), modbusClientSolWest, tabAdNameSol, tabAdSol, tabAd10Sol, dataWest, listSolW);
                modbusClientSolWest.Disconnect();
            } catch (Exception e) {
                for (int i = 0; i < tabAdNameSol.length; i++) {
                    dataWest.put(tabAdNameSol[i], 0);
                }
                dataWest.put("Power", 0);
                listSolW.add(dataEast);
                obj.put(prop.getProperty("nameSolWest"), listSolW);
                logger.error("SolWest", e);
            }

            obj.put("Date", list);
            FileWriter file = new FileWriter(prop.getProperty("pathDirFile") + "ups.json");
            file.write(obj.toJSONString());// Ecrit l'objet dans le fichier Json
            file.flush();

        } catch (IOException e) {
            logger.error("Error Json File", e);
        }
    }

    /**
     *
     * @return le path du Json, actuellement inutilisé
     */
    public String setPath() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            pathDir = prop.getProperty("pathDirFile") + "ups.json";
            input.close();
        } catch (IOException i) {
            logger.error("properties", i);
        }
        return pathDir;
    }

    /**
     * Cette méthode fait la somme et la multiplication par 10 de la puissance produite par phase
     *
     * @param mod, Cette variable permet de le faire sur les deux onduleurs solaires
     * @return
     */
    private double threePhasesSol(ModbusClient mod) {
        int valL1 = 0;
        int valL2 = 0;
        int valL3 = 0;
        double threePhases;
        try {
            valL3 = mod.ReadHoldingRegisters(24, 1)[0];
            valL1 = mod.ReadHoldingRegisters(25, 1)[0];
            valL2 = mod.ReadHoldingRegisters(26, 1)[0];
        } catch (ModbusException | IOException m) {
            logger.error("ModbusPowerSolarFunction", m);
        }
        threePhases = (valL1 + valL2 + valL3) * 10;
        return threePhases;

    }

    /**
     * @param name, Nom des onduleurs
     * @param mod, connection modbus aux onduleurs
     * @param tabNomVar,Nom variable de soit sps soit solaire
     * @param tabAdrVar, Adresse variable de soit sps soit solaire
     * @param tabAdr10Var, Divise par 10 comme indiqué dans la documentation
     * @param data, Objet de chaque onduleur
     * @param liste, Liste de chaque onduleur
     */
    private void dataUPS(String name, ModbusClient mod, String[] tabNomVar, String[] tabAdrVar, String[] tabAdr10Var, JSONObject data, JSONArray liste) {
        tabAdArduino = prop.getProperty("arduinoNomVariable").split(",");
        try {
            for (int i = 0; i < tabAdrVar.length; i++) {
                valeur = mod.ReadHoldingRegisters(Integer.parseInt(tabAdrVar[i]), 1)[0];
                //Permet de diviser la valeur par 10 pour obtenir la valeur réelle et lisible
                for (String add : tabAdr10Var) {
                    if (tabAdrVar[i].equals(add)) {
                        valeur = valeur / 10;
                        if (tabAdrVar[i].equals("23")) {
                            valeur = valeur / 10;
                        }
                    }
                }
                data.put(tabNomVar[i], valeur);
            }
            //Dans le cas ou ce n'est pas le SPS, on utilise la methode Power
            if (!name.equals(prop.getProperty("nameSPS"))) {
                data.put("Power", threePhasesSol(mod));
            }
            //Dans le cas ou c'est le SPS on lit le fichier Arduino crée par le csv et on fait la moyenne pour OUTSPS
            if (name.equals(prop.getProperty("nameSPS"))) {
                int VA1, VA2, VA3 = 0;
                float VA = 0;
                try {
                    //Reading the file created by the csv one
                    FileReader arduinoFile = new FileReader("/home/pi/CentraleSolaireData/Programmes/majika-drive-sample-1.0/bin/arduino");
                    BufferedReader arduinoB = new BufferedReader(arduinoFile);
                    String[] array = arduinoB.readLine().split(",");
                    int i = 0;
                    for (String compteur : array) {
                        data.put(tabAdArduino[i], compteur);
                        i++;
                    }
                    arduinoB.close();
                    arduinoFile.close();

                } catch (Exception e) {
                    logger.error("Reading File", e);
                }
                VA1 = mod.ReadHoldingRegisters(37, 1)[0];
                VA2 = mod.ReadHoldingRegisters(38, 1)[0];
                VA3 = mod.ReadHoldingRegisters(39, 1)[0];
                VA = (VA1 + VA2 + VA3) / 3;
                data.put("OUTSPS", VA);
                try {// Dans le SPS on ajoute l'execution de la commande de temperature du raspberry Pi pour avoir un Visuel sur son fonctionnement
                    Process i = Runtime.getRuntime().exec("cat /sys/class/thermal/thermal_zone0/temp");
                    BufferedReader rp = new BufferedReader(new InputStreamReader(i.getInputStream()));
                    double returned = Double.parseDouble(rp.readLine()) / 1000;
                    data.put("RaspBerry Pi Temperature", returned);
                } catch (Exception e) {
                    logger.error("fail to get RPBI temperature", e);
                }

            }

            liste.add(data);
            obj.put(name, liste);
        } catch (ModbusException m) {
            logger.error("Modbus Exception", m);
        } catch (IOException m) {
            logger.error("IoException", m);
        }
    }
}
