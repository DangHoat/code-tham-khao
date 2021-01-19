package gps.classes;

import java.util.ArrayList;
import java.util.List;

public class ResponseLocations {
    List<Location> locations;
    
    public ResponseLocations() {
        this.locations = new ArrayList<>();
    }
    public void addLocation(Location localtion) {
        this.locations.add(localtion);
    }
    public Object[] toArray() {
        return this.locations.toArray();
    }
}
