package gps.classes;

public class Location {
    private double longitude;
    private double latitude;
    private String date_time;
    
    public double getLongitude() {
        return this.longitude;
    }
    public double getLatitude() {
        return this.latitude;
    }
    public String getDateTime() {
        return this.date_time;
    }
    
    public Location(double longitude, double latitude, String date_time) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.date_time = new String(date_time);
    }
}
