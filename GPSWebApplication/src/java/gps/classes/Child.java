package gps.classes;

public class Child {
    private String id;
    private String name;
    
    public Child(String id, String name) {
        this.id = new String(id);
        this.name = new String(name);
    }
    public String getID() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
}