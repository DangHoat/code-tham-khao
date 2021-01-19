package gps.classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class GPSDatabase {
    
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://localhost/gpsdatabase";

    //  Database credentials
    static final String USER = "gps";
    static final String PASS = "gps123";
    
    // gps database tables
    public static final String DATABASE_TABLE_ACCOUNT = "ACCOUNT";
    public static final String DATABASE_TABLE_USER_INFO = "CHILD_INFO";
    public static final String DATABASE_TABLE_TRACKED_INFO = "TRACKED_INFO";
    public static final String DATABASE_TABLE_LOGIN_SESSION = "LOGIN_SESSION";
    
    // gps table columns
    public static final String[] DATABASE_TABLE_ACCOUNT_COLUMNS = { "USERNAME", "PASSWORD" };
    public static final String[] DATABASE_TABLE_TRACKED_INFO_COLUMNS = {"KEY_ID_USER", "LONGITUDE", "LATITUDE", "DATE_TIME", "SEEN" };
    public static final String[] DATABASE_TABLE_USER_INFO_COLUMNS = { "KEY_USER", "USERNAME", "CHILD_NAME" };
    public static final String[] DATABASE_TABLE_LOGIN_SESSION_COLUMNS = { "USERNAME", "SESSION_ID"};
    
    Connection conn = null;
    Statement stmt = null;
    
    public GPSDatabase() {
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        }
        catch(Exception ex) {
            GPS.printMessageToConsole(ex.getMessage());
        }
    }
    
    private static String convertArraytoString(String[] strings)
    {
        String command = "";
        for (int i = 0; i < strings.length; i++)
        {
            command += strings[i];
            if (i < strings.length - 1)
                command += ",";
        }
        return command;
    }
    
    public ResultSet query(String table_name, String[] columns, String condition) {
        
        if(table_name == null || table_name.equals(""))
            return null;
        
        try {
            stmt = conn.createStatement();
            
            String sql;
            sql = "SELECT";
            // columns
            if(columns == null)
                sql += " *"; // SELECT *
            else {
                sql += " " + convertArraytoString(columns); // SELECT col1, col2
            }
            sql += " FROM " + table_name; // SELECT col1, col2 FROM TABLE_NAME
            if(condition != null)
                sql += " WHERE " + condition;
            
            ResultSet rs = stmt.executeQuery(sql);
            return rs;
        }
        catch(Exception ex) {
            GPS.printMessageToConsole(ex.getMessage());
        }
        
        return null;
    }
    
    /*
        -1: unsuccess
    */
    public int insert(String table_name, String[] columns, String[] values) {
        
        if(table_name == null || values == null || table_name.equals(""))
            return -1;
        try {
            stmt = conn.createStatement();
            String sql;
            sql = "INSERT INTO ";
            sql += table_name;
            sql += String.format(" ( %s ) ", convertArraytoString(columns));
            sql += String.format("VALUES ( %s );", convertArraytoString(values));
            
            return stmt.executeUpdate(sql);
        }
        catch(Exception ex) {
            GPS.printMessageToConsole(ex.getMessage());
            return -1;
        }
    }
    
    public int delete(String table_name, String conditions) {
        
        if(table_name == null)
            return -1;
        
        try {
            stmt = conn.createStatement();
            String sql;
            sql = "DELETE FROM ";
            sql += table_name;
            if(conditions != null)
                sql += " WHERE " + conditions;
            
            return stmt.executeUpdate(sql);
        }
        catch(Exception ex) {
            GPS.printMessageToConsole(ex.getMessage());
            return -1;
        }
    }
    
    public int update(String table_name, String[] columns, String[] values, String condition) {
        if(table_name == null || columns == null || values == null || columns.length != values.length)
            return -1;
        
        try {
            stmt = conn.createStatement();
            String sql = "UPDATE " + table_name + " SET "; // "update table_name set "
            for(int i = 0; i < columns.length; i++) {
                sql += String.format("%s=%s", columns[i], values[i]);
                if(i < columns.length - 1)
                    sql += ",";
            }
            if(condition != null)
                sql += String.format(" WHERE %s",condition);
            return stmt.executeUpdate(sql);
        }
        catch (Exception ex) {
            GPS.printMessageToConsole(ex.getMessage());
            return -1;
        }
    }
    
    public boolean exist(String table_name, String condition) {
        
        try {
            ResultSet rs = this.query(table_name, null , condition);
            if(rs != null) {
                if(rs.first())
                    return true;
            }
        }
        catch(Exception ex) { }
        return false;
    }
    
}
