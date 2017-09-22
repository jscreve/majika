package org.majika.monitoring.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.majika.monitoring.ftp.AppFtp;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class CommandHelper {

    private static Logger logger = LogManager.getLogger(CommandHelper.class);

    private static final String SHELL_FILE = "command.sh";

    public String executeCommand(String command) {
        logger.info("Running command : " + command);
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            System.out.println(command);
            p = Runtime.getRuntime().exec(command);
            p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            logger.error(e);
        }

        return output.toString();
    }

    public String executeShellCommand(String command) {
        logger.info("Running command : " + command);
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            //put command in a shell script
            String path = System.getProperty("user.home") + "/" + SHELL_FILE;
            FileWriter fw = new FileWriter(path);
            fw.write(command);
            fw.close();


            //execute command
            //put command in a shell script
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", path);
            p = pb.start();
            p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            logger.error(e);
        }

        return output.toString();
    }
}
