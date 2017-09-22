package org.majika.monitoring;

import org.majika.monitoring.csv.ApplicationCsv;
import org.majika.monitoring.ftp.AppFtp;
import org.majika.monitoring.ftpZip.AppFtpZip;
import org.majika.monitoring.logs.RemoteLogManager;
import org.majika.monitoring.command.CommandManager;
import org.majika.monitoring.updateProgram.ProgramUpdate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * Created by etienne on 28/07/2017.
 */

public class Application {
    private static Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        CommandManager commandManager;
        if(args.length > 0) {
            switch (args[0]) {
                case "csv":
                    ApplicationCsv applicationCsv = new ApplicationCsv();
                    applicationCsv.fetchData();
                    break;
                case "ftp":
                    AppFtp appFtp = new AppFtp();
                    appFtp.executeFTPCommand();
                    break;
                case "ftpZip":
                    AppFtpZip appFtpZip = new AppFtpZip();
                    appFtpZip.init(new Date());
                    appFtpZip.executeFtpZipCommand();
                    break;
                case "updateProgram":
                    ProgramUpdate programUpdate = new ProgramUpdate();
                    programUpdate.executeUpdateCommand();
                    break;
                case "uploadLogs":
                    RemoteLogManager remoteLogManager = new RemoteLogManager();
                    remoteLogManager.writeConnectionStat();
                    remoteLogManager.sendLogs();
                    break;
                case "command":
                    commandManager = new CommandManager();
                    commandManager.executeCommandFromFTP();
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
