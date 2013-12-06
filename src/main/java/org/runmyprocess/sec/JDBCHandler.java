package org.runmyprocess.sec;

import com.runmyprocess.sec.Config;
import com.runmyprocess.sec.GenericHandler;
import com.runmyprocess.sec.SECErrorManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class JDBCHandler {

    public static void main(String [] args)throws IOException {

        try{
            GenericHandler genericHandler = new GenericHandler();//Creates a new instance of generic handler
            System.out.println( "Searching for config file..." );
             Config conf = new Config("configFiles"+File.separator+"handler.config",true);//sets the congif info
            System.out.println( "Handler config file found for manager ping port " + conf.getProperty("managerPort"));
            genericHandler.run( conf);//Runs the handler
        }catch( Exception e ){
            SECErrorManager errorManager = new SECErrorManager();//creates a new instance of the SDK error manager
            errorManager.logError(e.getMessage(), Level.SEVERE);//logs the error
            e.printStackTrace();//prints the error stack trace
        }
    }

}
