package cmdline;

import cmdline.csv.ApplicationCsv;
import cmdline.ftp.AppFtp;
import cmdline.ftpZip.AppFtpZip;
import cmdline.json.DriveSample;
import cmdline.test.TestJava;
import cmdline.updateProgram.ProgramUpdate;
import cmdline.zip.DriveSampleZip;

import java.io.IOException;

/**
 * Created by etienne on 28/07/2017.
 */
public class Application {
    public static void main(String[] args) {
        try {
            switch (args[0]) {
                case "csv":
                    new ApplicationCsv();
                    break;
                case "ftp":
                    new AppFtp();
                    break;
                case "zip":
                    new DriveSampleZip();
                    break;
                case "test":
                    new TestJava();
                    break;
                case "ftpZip":
                    new AppFtpZip();
                    break;
                case "updateProgram":
                    new ProgramUpdate();
                    break;
                default:
                    System.out.println("This is not a proper arguments");
                    break;
            }
        } catch (IOException i) {
            System.out.println(i);
        }
    }
}
