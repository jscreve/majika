package org.majika.monitoring.csv;

/**
 * Created by etienne on 28/07/2017.
 */

import de.re.easymodbus.modbusclient.ModbusClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class ApplicationCsv {
    private Properties prop = new Properties();
    private static Logger logger = LogManager.getLogger(ApplicationCsv.class);



    public static void main(String[] args) {
        new ApplicationCsv();

    }

    public ApplicationCsv() {
        init();
    }

    /**
     *Cette classe a pour objectif de configurer les variables obtenue à partir du config.properties et de lancer la classe CSVHandler avec ces variables
     */

    private void init() {
        //Initialisation Connection Modbus TCP/IP, pour relever les données
        ModbusClient modbusClientSolEast;
        ModbusClient modbusClientSolWest;
        ModbusClient modbusClientSPS;

        //Initialisation des variables utilisées pour convertir les données du config.properties
        String[] tabNVarSol;
        String[] tabNVarSPS;
        String[] tabAdSol;
        String[] tabAdSPS;
        String[] tabAd10SPS;
        String[] tabAd10Sol;
        try {
            //La ligne ci dessous permet d'avoir accès directement dans le lib au conf.properties sans indiquer le path
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            //Conversion des différentes lignes reçu par le config.properties en tableau(l'utilisation est plus simple
            tabNVarSol = prop.getProperty("tabNomVariableSolCsv").split(",");
            tabNVarSPS = prop.getProperty("tabNomVariableSPSCsv").split(",");
            tabAdSol = prop.getProperty("tabAdresseSolCsv").split(",");
            tabAdSPS = prop.getProperty("tabAdresseSPSCsv").split(",");
            tabAd10SPS = prop.getProperty("divisePar10SPS").split(",");
            tabAd10Sol = prop.getProperty("divisePar10Sol").split(",");

            //Initialisation pour la connection aux 3 onduleurs en indiquant leur adresse IP et le port de connexion
            modbusClientSolEast = new ModbusClient(prop.getProperty("ipAdresseSolEast"), 502);
            modbusClientSolWest = new ModbusClient(prop.getProperty("ipAdresseSolWest"), 502);
            modbusClientSPS = new ModbusClient(prop.getProperty("ipAdresseSPS"), 502);

            //Initialisation des fichiers .csv à créer
            CsvHandler UPS = new CsvHandler();

            //Connection aux onduleurs et relevé des data en utilisant le methode setFileCsv qui regroupe toutes les autres methodes
            UPS.setFileCsv(prop.getProperty("pathDirFile"), prop.getProperty("pathArduinoPython"), tabNVarSPS, tabNVarSol, modbusClientSPS, modbusClientSolEast, modbusClientSolWest, tabAdSPS, tabAdSol, tabAd10SPS, tabAd10Sol);

            //Fermeture de la lecture du conf.properties
            input.close();
        } catch (Exception e1) {
            logger.error("Error in init() : ", e1);
        }
    }

}
