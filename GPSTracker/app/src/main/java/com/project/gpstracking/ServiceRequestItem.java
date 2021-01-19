package com.project.gpstracking;

public class ServiceRequestItem {

    private String session_id;
    private String child_id;
    private int time;
    private int distance;

    public ServiceRequestItem(String session_id, String child_id, int time, int distance) {
        this.session_id = session_id;
        this.child_id = child_id;
        this.time = time;
        this.distance = distance;
    }

    public String getSessionId() {
        return this.session_id;
    }
    public String getChidId() {
        return this.child_id;
    }
    public int getTime() {
        return this.time;
    }
    public int getDistance() {
        return this.distance;
    }
}
