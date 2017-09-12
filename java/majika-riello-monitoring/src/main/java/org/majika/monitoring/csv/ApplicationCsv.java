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
    private String tabNomVariableSolCsv;
    private String tabNomVariableSPSCsv;
    private String tabAdresseSolCsv;
    private String tabAdresseSPSCsv;
    private String divisePar10SPS;
    private String divisePar10Sol;
    private String ipAdresseSolEast;
    private String ipAdresseSolWest;
    private String ipAdresseSPS;
    private String pathArduinoPython;
    private String pathDirFile;

    //Initialisation des fichiers .csv à créer
    CsvHandler UPS = new CsvHandler();

    public static void main(String[] args) {
        new ApplicationCsv();
    }

    public ApplicationCsv() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
        try {
            prop.load(input);
            input.close();
            tabNomVariableSolCsv = prop.getProperty("tabNomVariableSolCsv");
            tabNomVariableSPSCsv = prop.getProperty("tabNomVariableSPSCsv");
            tabAdresseSolCsv = prop.getProperty("tabAdresseSolCsv");
            tabAdresseSPSCsv = prop.getProperty("tabAdresseSPSCsv");
            divisePar10SPS = prop.getProperty("divisePar10SPS");
            divisePar10Sol = prop.getProperty("divisePar10Sol");
            ipAdresseSolEast = prop.getProperty("ipAdresseSolEast");
            ipAdresseSolWest = prop.getProperty("ipAdresseSolWest");
            ipAdresseSPS = prop.getProperty("ipAdresseSPS");
            pathArduinoPython = prop.getProperty("pathArduinoPython");
            pathDirFile = prop.getProperty("pathDirFile");
            if (!pathDirFile.endsWith("/")) {
                pathDirFile += "/";
            }
        } catch(IOException e) {
            logger.error("Error in properties loading", e);
        }
    }

    /**
     *Cette classe a pour objectif de configurer les variables obtenue à partir du config.properties et de lancer la classe CSVHandler avec ces variables
     */
    public void fetchData() {
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
            //Conversion des différentes lignes reçu par le config.properties en tableau(l'utilisation est plus simple
            tabNVarSol = tabNomVariableSolCsv.split(",");
            tabNVarSPS = tabNomVariableSPSCsv.split(",");
            tabAdSol = tabAdresseSolCsv.split(",");
            tabAdSPS = tabAdresseSPSCsv.split(",");
            tabAd10SPS = divisePar10SPS.split(",");
            tabAd10Sol = divisePar10Sol.split(",");

            //Initialisation pour la connection aux 3 onduleurs en indiquant leur adresse IP et le port de connexion
            modbusClientSolEast = new ModbusClient(ipAdresseSolEast, 502);
            modbusClientSolWest = new ModbusClient(ipAdresseSolWest, 502);
            modbusClientSPS = new ModbusClient(ipAdresseSPS, 502);

            //Initialisation des fichiers .csv à créer
            CsvHandler UPS = new CsvHandler();

            //Connection aux onduleurs et relevé des data en utilisant le methode setFileCsv qui regroupe toutes les autres methodes
            UPS.setFileCsv(pathDirFile, pathArduinoPython, tabNVarSPS, tabNVarSol, modbusClientSPS, modbusClientSolEast, modbusClientSolWest, tabAdSPS, tabAdSol, tabAd10SPS, tabAd10Sol);
        } catch (Exception e1) {
            logger.error("Error in init() : ", e1);
        }
    }
}