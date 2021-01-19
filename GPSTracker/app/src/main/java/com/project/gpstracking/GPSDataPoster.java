package com.project.gpstracking;

import com.google.gson.Gson;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.AndroidHttpTransport;

public class GPSDataPoster {

    private static final String NAMESPACE = "http://webservice.gps/";
    private static final String URL = "http://192.168.56.1:8080/GPSWebApplication/GPSWebService?WSDL";

    public static final String METHOD_GET_CHILDREN = "getChildren";
    public static final String METHOD_LOGIN = "login";
    public static final String METHOD_LOGOUT = "logout";
    public static final String METHOD_REGISTER_ACCOUNT = "registerAccount";
    public static final String METHOD_POST_LOCATION = "postLocation";
    public static final String METHOD_ADD_CHILD = "addChild";
    public static final String METHOD_GET_LOCATIONS = "getLocations";

    private String method_name;
    private String soap_action;
    private SoapObject request;

    private Gson gson = new Gson();

    public GPSDataPoster(String method_name) {
        this.method_name = method_name;
        this.soap_action = NAMESPACE + method_name;
        request = new SoapObject(NAMESPACE, method_name);
    }

    public void addProperty(String name, String value, Object objects) {
        PropertyInfo pi = new PropertyInfo();
        pi.setName(name);
        pi.setValue(value);
        pi.setType(objects);
        request.addProperty(pi);
    }

    public Object post() {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        AndroidHttpTransport androidHttpTransport = new AndroidHttpTransport(URL);
        try {
            androidHttpTransport.call(soap_action, envelope);
            Object obj = envelope.getResponse();
            return obj;
        } catch (Exception ex) {
            ex.printStackTrace();
            return gson.toJson(new ResponseMessage(-1, "Error input type."));
        }
    }

}
