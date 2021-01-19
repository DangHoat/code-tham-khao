package gps.classes;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;

public class GPS {
    
    protected static final String INVALID_USERNAME_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz_.";
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    
    /* password is a hex code */
    public static final String INVALID_PASSWORD_CHARS = "0123456789abcdef";
    
    public static boolean isValidUsername(String username)
    {
        username = username.toLowerCase();
        for(int i = 0; i < username.length(); i ++)
            if (GPS.INVALID_USERNAME_CHARS.indexOf(username.charAt(i)) < 0)
                return false;
        return true;
    }
    
    public static boolean isValidPassword(String password)
    {
        password = password.toLowerCase();
        for(int i = 0; i < password.length(); i ++)
            if (GPS.INVALID_PASSWORD_CHARS.indexOf(password.charAt(i)) < 0)
                return false;
        return true;
    }
    
    public static void printMessageToConsole(String message) {
        System.out.println("[" + new Date().toLocaleString() + "] " + message);
    }
    
    public static String hashToSHA256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            return bytesToHex(digest);
        }
        catch(Exception ex) {
            GPS.printMessageToConsole(ex.getMessage());
            return null;
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        String re = "";
        for (int i = 0; i < bytes.length; i ++) {
            re += String.format("%02x", bytes[i]);
        }
        return re;
    }
    
}
