package gps.webservice;

import com.google.gson.Gson;
import gps.classes.GPS;
import gps.classes.Child;
import gps.classes.GPSDatabase;
import gps.classes.ResponseChildren;
import gps.classes.ResponseLocations;
import gps.classes.Location;
import gps.classes.ResponseMessage;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

@WebService(serviceName = "GPSWebService")
public class GPSWebService {

    private GPSDatabase gps_database = new GPSDatabase();
    Gson gson = new Gson();
    
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        return "Hello " + txt + " !";
    }

    /**
     * Web service operation
     * 
     * -1: tracked
     * 1: tracker
     */
    @WebMethod(operationName = "login")
    public String login(
            @WebParam(name = "username") String username, 
            @WebParam(name = "password") String password) {
        
        if(gps_database != null) {
            try {
                /* valid username */
                if(GPS.isValidUsername(username)) {
                    String hashedPass = GPS.hashToSHA256(password.getBytes());
                    ResultSet rs = gps_database.query(GPSDatabase.DATABASE_TABLE_ACCOUNT,
                            null ,
                            String.format("%s='%s' AND %s='%s'", GPSDatabase.DATABASE_TABLE_ACCOUNT_COLUMNS[0], username,
                                    GPSDatabase.DATABASE_TABLE_ACCOUNT_COLUMNS[1], hashedPass));
                    if(rs != null) {
                        /* login success */
                        if(rs.first()) {
                            String time = Calendar.getInstance().getTime().toGMTString();
                            String hashString = username + time;
                            String session_id = GPS.hashToSHA256(
                                    hashString.getBytes());
                            int check = -1;
                            check = gps_database.insert(
                                GPSDatabase.DATABASE_TABLE_LOGIN_SESSION,
                                        GPSDatabase.DATABASE_TABLE_LOGIN_SESSION_COLUMNS,
                                        new String[] {"'" + username + "'",
                                            "'" + session_id + "'"});
                            if(check < 1) {
                                ResponseMessage msg = new ResponseMessage(-1, "Error when logining.");
                                return gson.toJson(msg);
                            }
                            ResponseMessage msg = new ResponseMessage(0, session_id);
                            return gson.toJson(msg);
                        }
                        /* login unsuccess */
                        else {
                            ResponseMessage msg = new ResponseMessage(-1, "Invalid username or password.");
                            return gson.toJson(msg);
                        }
                    }
                    /* login unsuccess */
                    else {
                        ResponseMessage msg = new ResponseMessage(-1, "Invalid username or password.");
                        return gson.toJson(msg);
                    }

                }
                /* invalid username */ 
                else {
                    ResponseMessage msg = new ResponseMessage(-1, "Invalid username or password.");
                    return gson.toJson(msg);
                }
            }
            catch(Exception ex) {
                ResponseMessage msg = new ResponseMessage(-1, ex.getMessage());
                return gson.toJson(msg);
            }
        }
        /* can not create database */
        else {
            ResponseMessage msg = new ResponseMessage(-1, "Server is not available now.");
            return gson.toJson(msg);
        }
    }
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "registerAccount")
    public String registerAccount(
            @WebParam(name = "username") String username, 
            @WebParam(name = "password") String password) {
        
        if(username.length() > 49) {
            ResponseMessage msg = new ResponseMessage(-1, "Invalid username.");
            return gson.toJson(msg);
        }
        
        if(gps_database != null) {
            try {
                /* valid username */
                if(GPS.isValidUsername(username)) {
                    ResultSet rs = gps_database.query(GPSDatabase.DATABASE_TABLE_ACCOUNT,
                            null ,
                            String.format("%s='%s'", GPSDatabase.DATABASE_TABLE_ACCOUNT_COLUMNS[0], username));
                    if(rs != null) {
                        /* username exists */
                        if(rs.first()) {
                            ResponseMessage msg = new ResponseMessage(-1, "Username exists.");
                            return gson.toJson(msg);
                        }
                        /* username not exists */
                        else {
                            if(gps_database.insert(
                            GPSDatabase.DATABASE_TABLE_ACCOUNT,
                                    GPSDatabase.DATABASE_TABLE_ACCOUNT_COLUMNS,
                                    new String[] {"'" + username + "'",
                                        "'" + GPS.hashToSHA256(password.getBytes()) + "'"}) > 0) {
                                ResponseMessage msg = new ResponseMessage(0, "Register sucessfully.");
                                return gson.toJson(msg);
                            }
                            else {
                                ResponseMessage msg = new ResponseMessage(-1, "Register unsucessfully.");
                                return gson.toJson(msg);
                            }
                        }
                    }
                    /* fail */
                    else {
                        ResponseMessage msg = new ResponseMessage(-1, "Could not register now.");
                        return gson.toJson(msg);
                    }

                }
                /* invalid username */ 
                else {
                    ResponseMessage msg = new ResponseMessage(-1, "Invalid username. Username is in a->z 0->9 ._");
                    return gson.toJson(msg);
                }
            }
            catch(Exception ex) {
                ResponseMessage msg = new ResponseMessage(-1, ex.getMessage());
                return gson.toJson(msg);
            }
        }
        /* can not create database */
        else {
            ResponseMessage msg = new ResponseMessage(-1, "Server is not available now.");
            return gson.toJson(msg);
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "logout")
    public String logout(@WebParam(name = "session_id") String session_id) {
        
        if(gps_database != null) {
            try {
                int check = -1;
                String conditions = String.format("SESSION_ID='%s'", session_id);
                
                check = gps_database.delete(GPSDatabase.DATABASE_TABLE_LOGIN_SESSION, conditions);
                if(check == -1) {
                    ResponseMessage msg = new ResponseMessage(-1, "Can not logout now.");
                    return gson.toJson(msg);
                }
                
                ResponseMessage msg = new ResponseMessage(0, "Logout successfully.");
                return gson.toJson(msg);
            }
            catch(Exception ex) {
                ResponseMessage msg = new ResponseMessage(-1, ex.getMessage());
                return gson.toJson(msg);
            }
        }
        /* can not create database */
        else {
            ResponseMessage msg = new ResponseMessage(-1, "Server is not available now.");
            return gson.toJson(msg);
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "addChild")
    public String addChild(@WebParam(name = "session_id") String session_id,
            @WebParam(name = "username") String username,
            @WebParam(name = "child_name") String child_name) {
        
        if(gps_database != null) {
            try {
                String condition = String.format("%s='%s' AND %s='%s'", 
                        GPSDatabase.DATABASE_TABLE_LOGIN_SESSION_COLUMNS[0], username,
                        GPSDatabase.DATABASE_TABLE_LOGIN_SESSION_COLUMNS[1], session_id);
                /* session exits */
                if(gps_database.exist(GPSDatabase.DATABASE_TABLE_LOGIN_SESSION, condition)) {
                    String key_child_id = username.toLowerCase().replaceAll(" ", "") +
                            child_name.toLowerCase().replaceAll(" ", "");
                    int check = gps_database.insert(GPSDatabase.DATABASE_TABLE_USER_INFO,
                            GPSDatabase.DATABASE_TABLE_USER_INFO_COLUMNS,
                            new String[] {"'" + key_child_id + "'",
                                "'" + username + "'", 
                                "'" + child_name + "'"});
                    /* add successfully */
                    if(check != -1) {
                        ResponseMessage msg = new ResponseMessage(0, "Added child");
                        return gson.toJson(msg);
                    }
                    /* added unsuccessfully */
                    else {
                        ResponseMessage msg = new ResponseMessage(-1, "Can not add child. Choose another name.");
                        return gson.toJson(msg);
                    }
                }
                /* session not exist */
                else {
                    ResponseMessage msg = new ResponseMessage(-1, "You must login first.");
                    return gson.toJson(msg);
                }
            }
            catch(Exception ex) {
                ResponseMessage msg = new ResponseMessage(-1, ex.getMessage());
                return gson.toJson(msg);
            }
        }
        /* can not create database */
        else {
            ResponseMessage msg = new ResponseMessage(-1, "Server is not available now.");
            return gson.toJson(msg);
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getChildren")
    public String getChildren(@WebParam(name = "session_id") String session_id,
            @WebParam(name = "username") String username) {
        
        if(gps_database != null) {
            try {
                String condition = String.format("%s='%s' AND %s='%s'", 
                        GPSDatabase.DATABASE_TABLE_LOGIN_SESSION_COLUMNS[0], username,
                        GPSDatabase.DATABASE_TABLE_LOGIN_SESSION_COLUMNS[1], session_id);
                /* session exits */
                if(gps_database.exist(GPSDatabase.DATABASE_TABLE_LOGIN_SESSION, condition)) {
                    condition = String.format("%s='%s'",
                            GPSDatabase.DATABASE_TABLE_USER_INFO_COLUMNS[1],
                            username);
                    ResultSet rs = gps_database.query(GPSDatabase.DATABASE_TABLE_USER_INFO, 
                            null,
                            condition);
                    ResponseChildren children = new ResponseChildren();
                    if(rs != null) {
                        while(rs.next()) {
                            Child child = new Child(rs.getString(GPSDatabase.DATABASE_TABLE_USER_INFO_COLUMNS[0]),
                            rs.getString(GPSDatabase.DATABASE_TABLE_USER_INFO_COLUMNS[2]));
                            children.add(child);
                        }
                    }
                    //String str_children = gson.toJson(rs, ResponseChildren.class);
                    String str_children = gson.toJson(children);
                    ResponseMessage msg = new ResponseMessage(0, str_children);
                    return gson.toJson(msg);
                }
                /* session not exist */
                else {
                    ResponseMessage msg = new ResponseMessage(-1, "You must login first.");
                    return gson.toJson(msg);
                }
            }
            catch(Exception ex) {
                ResponseMessage msg = new ResponseMessage(-1, ex.getMessage());
                return gson.toJson(msg);
            }
        }
        /* can not create database */
        else {
            ResponseMessage msg = new ResponseMessage(-1, "Server is not available now.");
            return gson.toJson(msg);
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "postLocation")
    public String postLocation(@WebParam(name = "session_id") String session_id, 
            @WebParam(name = "key_id_user") String key_id_user, 
            @WebParam(name = "longitude") double longitude, 
            @WebParam(name = "latitude") double latitude,
            @WebParam(name = "date") String date) {
        
        if(gps_database != null) {
            try {
                String condition = "";
                
                /* get username */
                String username = "";
                condition = String.format("%s='%s'",
                        GPSDatabase.DATABASE_TABLE_USER_INFO_COLUMNS[0],
                        key_id_user);
                ResultSet rs = gps_database.query(GPSDatabase.DATABASE_TABLE_USER_INFO,
                        null, 
                        condition);
                while(rs.next()) {
                    username = rs.getString(GPSDatabase.DATABASE_TABLE_USER_INFO_COLUMNS[1]);
                }
                
                /* check session */
                condition = String.format("%s='%s' AND %s='%s'", 
                        GPSDatabase.DATABASE_TABLE_LOGIN_SESSION_COLUMNS[0], username,
                        GPSDatabase.DATABASE_TABLE_LOGIN_SESSION_COLUMNS[1], session_id);
                /* session exits */
                if(gps_database.exist(GPSDatabase.DATABASE_TABLE_LOGIN_SESSION, condition)) {
                    String[] values = { String.format("'%s'", key_id_user),
                        String.format("%s", longitude),
                        String.format("%s", latitude),
                        String.format("'%s'", date),
                        String.format("%s", 0)
                    };
                    int check = gps_database.insert(GPSDatabase.DATABASE_TABLE_TRACKED_INFO,
                            GPSDatabase.DATABASE_TABLE_TRACKED_INFO_COLUMNS,
                            values);
                    /* insert ok */
                    if(check != -1) {
                        ResponseMessage msg = new ResponseMessage(0, "Post successfully.");
                        return gson.toJson(msg);
                    }
                    else {
                        ResponseMessage msg = new ResponseMessage(-1, "Post unsuccessfully.");
                        return gson.toJson(msg);
                    }
                }
                /* session not exist */
                else {
                    ResponseMessage msg = new ResponseMessage(-1, "You must login first.");
                    return gson.toJson(msg);
                }
            }
            catch(Exception ex) {
                ResponseMessage msg = new ResponseMessage(-1, ex.getMessage());
                return gson.toJson(msg);
            }
        }
        /* can not create database */
        else {
            ResponseMessage msg = new ResponseMessage(-1, "Server is not available now.");
            return gson.toJson(msg);
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getLocations")
    public String getLocations(@WebParam(name = "session_id") String session_id, 
            @WebParam(name = "key_id_user") String key_id_user, 
            @WebParam(name = "seen") boolean seen, 
            @WebParam(name = "date") String date) {
        
        if(gps_database != null) {
            try {
                String condition = "";
                
                /* get username */
                String username = "";
                condition = String.format("%s='%s'",
                        GPSDatabase.DATABASE_TABLE_USER_INFO_COLUMNS[0],
                        key_id_user);
                ResultSet rs = gps_database.query(GPSDatabase.DATABASE_TABLE_USER_INFO,
                        null, 
                        condition);
                while(rs.next()) {
                    username = rs.getString(GPSDatabase.DATABASE_TABLE_USER_INFO_COLUMNS[1]);
                }
                
                /* check session */
                condition = String.format("%s='%s' AND %s='%s'", 
                        GPSDatabase.DATABASE_TABLE_LOGIN_SESSION_COLUMNS[0], username,
                        GPSDatabase.DATABASE_TABLE_LOGIN_SESSION_COLUMNS[1], session_id);
                /* session exits */
                if(gps_database.exist(GPSDatabase.DATABASE_TABLE_LOGIN_SESSION, condition)) {
                    ResponseLocations locations = new ResponseLocations();
                    /* get location */
                    condition = String.format("%s='%s'",
                            GPSDatabase.DATABASE_TABLE_TRACKED_INFO_COLUMNS[0], key_id_user);
                    
                    /* unread */
                    if(!seen) {
                        condition += String.format(" AND %s=%s", GPSDatabase.DATABASE_TABLE_TRACKED_INFO_COLUMNS[4], "0");
                    }
                    
                    if(date != null) {
                        /*if(!date_time.equals("")) {
                            condition += String.format(" AND %s='%s'",
                                    GPSDatabase.DATABASE_TABLE_TRACKED_INFO_COLUMNS[3], date_time);
                        }*/
                        try {
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            df.setLenient(false);
                            df.parse(date); // invalid datetime
                            condition += String.format(" AND DATE(%s)=STR_TO_DATE('%s', '%%Y-%%m-%%d')",
                                    GPSDatabase.DATABASE_TABLE_TRACKED_INFO_COLUMNS[3], date);
                        }
                        /* invalid datetime */
                        catch (Exception ex) {
                            ex.printStackTrace();}
                    }
                    /* get locations */
                    rs = gps_database.query(GPSDatabase.DATABASE_TABLE_TRACKED_INFO,
                            null,
                            condition);
                    if(rs != null) {
                        while(rs.next()) {
                            try {
                                Location location = new Location(
                                        Double.parseDouble(rs.getString(GPSDatabase.DATABASE_TABLE_TRACKED_INFO_COLUMNS[1])), 
                                        Double.parseDouble(rs.getString(GPSDatabase.DATABASE_TABLE_TRACKED_INFO_COLUMNS[2])),
                                        rs.getString(GPSDatabase.DATABASE_TABLE_TRACKED_INFO_COLUMNS[3]));
                                locations.addLocation(location);
                                int id = rs.getInt("ID");
                                
                                /* mark as read */
                                gps_database.update(GPSDatabase.DATABASE_TABLE_TRACKED_INFO, 
                                        new String[] {GPSDatabase.DATABASE_TABLE_TRACKED_INFO_COLUMNS[4]}, 
                                        new String[] {"1"}, 
                                        String.format("ID=%s", id));
                            }
                            catch(Exception ex) {
                                continue;
                            }
                        }
                    }
                    ResponseMessage msg = new ResponseMessage(0, gson.toJson(locations));
                    return gson.toJson(msg);
                }
                /* session not exist */
                else {
                    ResponseMessage msg = new ResponseMessage(-1, "You must login first.");
                    return gson.toJson(msg);
                }
            }
            catch(Exception ex) {
                ResponseMessage msg = new ResponseMessage(-1, ex.getMessage());
                return gson.toJson(msg);
            }
        }
        /* can not create database */
        else {
            ResponseMessage msg = new ResponseMessage(-1, "Server is not available now.");
            return gson.toJson(msg);
        }
    }
    
}
