package org.majika.monitoring.test;

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
            String tableau = "cest,moi,le,grand,etienne";
            String[] az  = tableau.split(",");
            for(int i=0; i<az.length; i++ ){
                if(az[i].equals("etienne")){
                    az[i]="Rinkan";


                }
                System.out.println(az[i]);
            }


            } catch(Exception e){System.out.println(e);}





    }


}
