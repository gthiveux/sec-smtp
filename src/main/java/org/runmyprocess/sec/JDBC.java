package org.runmyprocess.sec;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Properties;

import java.util.logging.Level;


import com.runmyprocess.sec.Config;
import com.runmyprocess.sec.ProtocolInterface;
import com.runmyprocess.sec.Response;
import com.runmyprocess.sec.SECErrorManager;
import org.runmyprocess.json.JSONArray;
import org.runmyprocess.json.JSONObject;


public class JDBC implements ProtocolInterface {


    private Response response = new Response();

    public JDBC() {
        // TODO Auto-generated constructor stub
    }

    private JSONObject DBAgentError(String error){

        response.setStatus(400);//sets the return status to internal server error
        JSONObject errorObject = new JSONObject();
        errorObject.put("error", error.toString());
        return errorObject;
    }

    private static final JSONObject resultSet2JSONObject(ResultSet rs) {
        JSONObject element = null;
        JSONArray joa = new JSONArray();
        JSONObject jo = new JSONObject();
        int totalLength = 0;
        ResultSetMetaData rsmd = null;
        String columnName = null;
        String columnValue = null;
        try {
            rsmd = rs.getMetaData();
            while (rs.next()) {
                element = new JSONObject();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    columnName = rsmd.getColumnName(i+1);
                    columnValue = rs.getString(columnName);
                    element.accumulate(columnName, columnValue);
                }
                joa.add(element);
                totalLength ++;
            }
            jo.accumulate("result", "success");
            jo.accumulate("rows", totalLength);
            jo.accumulate("data", joa);
        } catch (SQLException e) {
            jo.accumulate("result", "failure");
            jo.accumulate("error", e.getMessage());
        }
        return jo;
    }

    private Connection getConnection(String userName, String password, String sqlSource, String sqlDriver, String driverPath) throws Exception {

        System.out.println("Connecting to "+sqlSource);
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);
        URL[] urls = new URL[1];
        File f = new File(driverPath);
        urls[0] = f.toURL();
        URLClassLoader ucl = URLClassLoader.newInstance(urls);
        Driver driver = (Driver)Class.forName(sqlDriver, true, ucl).newInstance();
        DriverManager.registerDriver(new DriverShim(driver));
        conn = DriverManager.getConnection(sqlSource, userName, password);

        System.out.println("Connected to database");
        return conn;
    }

    private static JSONObject ExecuteStatement(Connection con, String sqlStatement)throws SQLException {

    // Get a statement from the connection
    Statement stmt = con.createStatement() ;
    JSONObject retObj = new JSONObject();
    // Execute the SQL
    if( stmt.execute(sqlStatement) == false )
    {
        // Get the update count
        String rep = "Query OK, "+ stmt.getUpdateCount() + " rows affected" ;
        retObj.put("Message",rep);
        System.out.println(rep) ;
    }
    else
    {
        // Get the result set and the metadata
        ResultSet         rs = stmt.getResultSet() ;
        retObj = resultSet2JSONObject(rs);

    }
    stmt.close() ;
    con.close() ;

    return retObj;

    }

    private JSONObject Execute(String driverPath, String sqlDriver, String sqlSource, String sqlUsername,String sqlPassword,String sqlStatement) throws Exception{

        Connection con = getConnection(sqlUsername, sqlPassword,sqlSource,sqlDriver,driverPath);
        return ExecuteStatement(con, sqlStatement);
    }

    @Override
    public void accept(JSONObject jsonObject,String configPath) {

        try {
            System.out.println ("Searching for config file...");
            Config conf = new Config("configFiles"+File.separator+ "JDBC.config",true);//sets the config info
            System.out.println( "Config file found  ");

            JSONObject prop = JSONObject.fromString(conf.getProperty(jsonObject.getString("DBType")));

            JSONObject DBData = Execute(prop.getString("sqlDriverPath"),prop.getString("sqlDriver"),prop.getString("sqlSource"),
                    jsonObject.getString("sqlUsername"),jsonObject.getString("sqlPassword"),
                    jsonObject.getString("sqlStatement")) ;

            response.setStatus(200);//sets the return status to 200
            JSONObject resp = new JSONObject();
            resp.put("DBData", DBData);//sends the info inside an object
            response.setData(resp);

        } catch (Exception e) {
            response.setData(this.DBAgentError(e.getMessage()));
            SECErrorManager errorManager = new SECErrorManager();
            errorManager.logError(e.getMessage(), Level.SEVERE);
            e.printStackTrace();
        }
    }

    @Override
    public Response getResponse() {
        return response;
    }

}
