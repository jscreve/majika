package cmdline.test;

import cmdline.csv.ApplicationCsv;

import java.io.*;

/**
 * Created by etienne on 03/08/2017.
 */
public class TestJava {

    public static void main(String[] args) {
        TestJava test = new TestJava();

    }
    public TestJava(){
        try {
            FileReader fr = new FileReader("C:\\Users\\etienne\\Documents\\StageDiego\\JSON\\UPS_14_08_2017.csv");
            BufferedReader r = new BufferedReader(fr);
            FileWriter fw = new FileWriter("C:\\Users\\etienne\\Documents\\StageDiego\\JSON\\UPS_14_08_2017.csv");
            BufferedWriter w = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(w);

            String line;
            while ((line = r.readLine()) != null) {
                String replaced = line.replace(".", ",");
                //replaced = replaced.replaceAll("[^A-Za-z0-9,]", ",");
                out.print(replaced);

            }
        }catch(Exception e){System.out.println(e);}





    }


}
