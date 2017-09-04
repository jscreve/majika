package org.majika.monitoring.ftp;


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


    private ModbusClient modbusClientSolEast;
    private ModbusClient modbusClientSolWest;
    private ModbusClient modbusClientSPS;


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

    private String[] tabAdSPS;
    private String[] tabAdSol;
    private String[] tabAd10SPS;
    private String[] tabAd10Sol;
    private String[] tabAdNameSPS;
    private String[] tabAdNameSol;
    private String[] array;
    private String[] tabAdArduino;

    /**
     * This is the main method called by the main class to create and get data in the json file
     */

    public void setFileJson() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input); // open the connection to the properties file
            tabAdSPS = prop.getProperty("tabAdresseSPSJson").split(",");
            tabAdSol = prop.getProperty("tabAdresseSolJson").split(",");
            tabAd10SPS = prop.getProperty("divisePar10SPS").split(",");
            tabAd10Sol = prop.getProperty("divisePar10Sol").split(",");
            tabAdNameSPS = prop.getProperty("tabNomVariableSPSJson").split(",");
            tabAdNameSol = prop.getProperty("tabNomVariableSolJson").split(",");
            modbusClientSolEast = new ModbusClient(prop.getProperty("ipAdresseSolEast"), 502);
            modbusClientSolWest = new ModbusClient(prop.getProperty("ipAdresseSolWest"), 502);
            modbusClientSPS = new ModbusClient(prop.getProperty("ipAdresseSPS"), 502);

            Date date = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("HH:mm dd.MM.yyyy");
            list.add(ft.format(date).toString());

            try {
                modbusClientSPS.Connect();
                dataUPS(prop.getProperty("nameSPS"), modbusClientSPS, tabAdNameSPS, tabAdSPS, tabAd10SPS, dataSPS, listSPS);
                modbusClientSPS.Disconnect();
            } catch (Exception e) {
                for (int i = 0; i < tabAdNameSPS.length; i++) {
                    dataSPS.put(tabAdNameSPS[i], 0);
                }
                dataSPS.put("OUTSPS", 0);
                listSPS.add(dataSPS);
                obj.put(prop.getProperty("nameSPS"), listSPS);
                logger.error("SPS", e);
            }

            try {
                modbusClientSolEast.Connect();
                dataUPS(prop.getProperty("nameSolEast"), modbusClientSolEast, tabAdNameSol, tabAdSol, tabAd10Sol, dataEast, listSolE);
                modbusClientSolEast.Disconnect();
            } catch (Exception e) {
                for (int i = 0; i < tabAdNameSol.length; i++) {
                    dataEast.put(tabAdNameSol[i], 0);
                }
                dataEast.put("Power", 0);
                listSolE.add(dataEast);
                obj.put(prop.getProperty("nameSolEast"), listSolE);
                //logger.error("SolEast",e);
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
            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e1) {
            logger.error("Error Json File", e1);
        }
    }

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
     * This method set the 3phases sum
     *
     * @param mod, this variable is used to do it on both solar Invertor
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
     * @param name
     * @param mod
     * @param tabNomVar
     * @param tabAdrVar
     * @param tabAdr10Var
     * @param data
     * @param liste
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
            if (!name.equals(prop.getProperty("nameSPS"))) {
                data.put("Power", threePhasesSol(mod));
            }
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
                try {
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
