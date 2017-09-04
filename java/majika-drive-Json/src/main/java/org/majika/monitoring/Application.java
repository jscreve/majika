package org.majika.monitoring;

import org.majika.monitoring.csv.ApplicationCsv;
import org.majika.monitoring.ftp.AppFtp;
import org.majika.monitoring.ftpZip.AppFtpZip;
import org.majika.monitoring.shell.ShellManager;
import org.majika.monitoring.test.TestJava;
import org.majika.monitoring.updateProgram.ProgramUpdate;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by etienne on 28/07/2017.
 */

public class Application {
    private static Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        ShellManager shellManager;
        if(args.length > 0) {
            switch (args[0]) {
                case "csv":
                    new ApplicationCsv();
                    break;
                case "ftp":
                    AppFtp appFtp = new AppFtp();
                    appFtp.executeFTPCommand();
                    break;
                case "zip":
                    //we don't store on Drive anymore
                    //new DriveSampleZip();
                    break;
                case "test":
                    new TestJava();
                    break;
                case "ftpZip":
                    AppFtpZip appFtpZip = new AppFtpZip();
                    appFtpZip.executeFtpZipCommand();
                    break;
                case "updateProgram":
                    ProgramUpdate programUpdate = new ProgramUpdate();
                    programUpdate.executeUpdateCommand();
                    //TODO temporary, remove
                    shellManager = new ShellManager();
                    shellManager.executeShellCommand();
                    //END TODO
                    break;
                case "shell":
                    shellManager = new ShellManager();
                    shellManager.executeShellCommand();
                    break;
                default:
                    logger.error("Wrong input argument");
                    break;
            }
        }
        else {
            logger.error("No input argument");
        }
    }
}
