package gps.classes;

import java.util.ArrayList;
import java.util.List;

/*
    ChildResponse
*/
public class ResponseChildren {
    private List<Child>children;
    
    public ResponseChildren() {
        this.children = new ArrayList<>();
    }
    public ResponseChildren(Child[] children) {
        this.children = new ArrayList<>();
        for(int i = 0; i < children.length; i++) {
            this.children.add(children[i]);
        }
    }
    
    public void add(Child child) {
        this.children.add(child);
    }
    public Object[] toArray() {
        return this.children.toArray();
    }
}