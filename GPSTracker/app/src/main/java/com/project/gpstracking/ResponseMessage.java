package com.project.gpstracking;

/*
    Response message for client
*/
public class ResponseMessage {

    // 0: success
    // 1: error
    private int response_code;
    private String message;
    
    public int getResponseCode() {
        return this.response_code;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public ResponseMessage(int response_code, String message) {
        this.message = new String(message);
        this.response_code = response_code;
    }
}

