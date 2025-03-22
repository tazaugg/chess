package server;

import com.google.gson.Gson;
import service.RespExp;
import model.AuthData;
import model.UserData;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    /** Clears the database */
    public void clear() throws RespExp {
        String path = "/db";
        makeRequest("DELETE", path, null, null, null);
    }

    /** Registers a new user and returns authentication data */
    public AuthData register(String username, String password, String email) throws RespExp {
        String path = "/user";
        Map<String, String> requestBody = Map.of(
                "username", username,
                "password", password,
                "email", email
        );
        return makeRequest("POST", path, requestBody, AuthData.class, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws RespExp {
        try {
            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if (authToken != null) {
                http.setRequestProperty("Authorization", "Bearer " + authToken);
            }
            writeRequestBody(request, http);
            http.connect();
            throwIfError(http);
            return readResponse(http, responseClass);
        } catch (Exception ex) {
            throw new RespExp(500, "Server request failed: " + ex.getMessage());
        }
    }

    private void writeRequestBody(Object request, HttpURLConnection http) throws Exception {
        if (request != null) {
            http.setRequestProperty("Content-Type", "application/json");
            String json = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(json.getBytes());
            }
        }
    }

    private void throwIfError(HttpURLConnection http) throws Exception {
        int status = http.getResponseCode();
        if (status / 100 != 2) {
            throw new RespExp(status, "Error: " + status);
        }
    }

    private <T> T readResponse(HttpURLConnection http, Class<T> responseClass) throws Exception {
        if (responseClass == null) return null;
        return new Gson().fromJson(new java.io.InputStreamReader(http.getInputStream()), responseClass);
    }
}
