package org.majika.monitoring.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.majika.monitoring.ftp.AppFtp;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandHelper {

    private static Logger logger = LogManager.getLogger(CommandHelper.class);

    public static String executeCommand(String command) {
        logger.info("Running command : " + command);
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            logger.error(e);
        }

        return output.toString();
    }
}
