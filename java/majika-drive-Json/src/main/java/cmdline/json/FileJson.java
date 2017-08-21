package cmdline.json;


import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.*;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class FileJson {


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
    public  String pathDir;
    private Properties prop = new Properties();
    private Logger logger = LogManager.getLogger();

    private String[] tabAdSPS;
    private String[] tabAdSol;
    private String[] tabAd10SPS;
    private String[] tabAd10Sol;
    private String[] tabAdNameSPS;
    private String[] tabAdNameSol;
    /**
     * This is the main method called by the main class to create and get data in the json file
     */

    public void setFileJson() {
        try {
            FileInputStream input = new FileInputStream("config.properties");
            prop.load(input); // open the connection to the properties file
            tabAdSPS = prop.getProperty("tabAdresseSPSJson").split(",");
            tabAdSol = prop.getProperty("tabAdresseSolJson").split(",");
            tabAd10SPS = prop.getProperty("divisePar10Sol").split(",");
            tabAd10Sol = prop.getProperty("tabAdresseSPSCsv").split(",");
            tabAdNameSPS = prop.getProperty("tabNomVariableSPSJson").split(",");
            tabAdNameSol = prop.getProperty("tabNomVariableSolJson").split(",");
            modbusClientSolEast = new ModbusClient(prop.getProperty("ipAdresseSolEast"), 502);
            modbusClientSolWest = new ModbusClient(prop.getProperty("ipAdresseSolWest"), 502);
            modbusClientSPS = new ModbusClient(prop.getProperty("ipAdresseSPS"), 502);

            Date date = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("HH:mm dd.MM.yyyy");
            list.add(ft.format(date).toString());

            modbusClientSPS.Connect();
            dataUPS(prop.getProperty("nameSPS"), modbusClientSPS, tabAdNameSPS, tabAdSPS, tabAd10SPS, dataSPS, listSPS);
            modbusClientSPS.Disconnect();

            modbusClientSolEast.Connect();
            dataUPS(prop.getProperty("nameSolEast"), modbusClientSolEast, tabAdNameSol, tabAdSol, tabAd10Sol, dataEast, listSolE);
            modbusClientSolEast.Disconnect();

            modbusClientSolWest.Connect();
            dataUPS(prop.getProperty("nameSolWest"), modbusClientSolWest, tabAdNameSol, tabAdSol, tabAd10Sol, dataWest, listSolW);
            modbusClientSolWest.Disconnect();

            obj.put("Date", list);
            FileWriter file = new FileWriter(prop.getProperty("pathDirFile") + "ups.json");
            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e1) {
            logger.error(e1);
        }
    }
    public String setPath() {
        try {
            FileInputStream input = new FileInputStream("config.properties");
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
        try {
            for (int i = 0; i < tabAdrVar.length; i++) {
                valeur = mod.ReadHoldingRegisters(Integer.parseInt(tabAdrVar[i]), 1)[0];
                //Permet de diviser la valeur par 10 pour obtenir la valeur réelle et lisible
                for (String add : tabAdr10Var) {
                    if (Integer.parseInt(tabAdrVar[i]) == Integer.parseInt(add)) {
                        valeur = valeur / 10;
                        if (Integer.parseInt(tabAdrVar[i]) == 23) {
                            valeur = valeur / 10;
                        }
                    }
                }
                data.put(tabNomVar[i], valeur);
            }
            if (!name.equals("UPS_SPS_Battery")) {
                data.put("Power", threePhasesSol(mod));
            }
            liste.add(data);
            obj.put(name, liste);
        } catch (ModbusException m) {
            logger.error(m);
        } catch (IOException m) {
            logger.error(m);
        }
    }
}
