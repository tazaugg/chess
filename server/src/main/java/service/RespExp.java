package service;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RespExp extends Exception {
    private final int statusCode;
    public RespExp(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
    public int statusCode() {
        return statusCode;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", statusCode));
    }

    public static RespExp fromJson(InputStream stream) {
        var map = new Gson().fromJson(new InputStreamReader(stream), HashMap.class);
        var status = ((Double)map.get("status")).intValue();
        String message = map.get("message").toString();
        return new RespExp(status, message);
    }
}
