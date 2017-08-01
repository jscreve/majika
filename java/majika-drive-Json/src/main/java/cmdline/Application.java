package cmdline;

import cmdline.csv.ApplicationCsv;
import cmdline.ftp.AppFtp;
import cmdline.json.DriveSample;
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
                default: System.out.println("This is not a proper arguments");
                        break;
            }
        }catch(IOException i){System.out.println(i);}
    }
}
