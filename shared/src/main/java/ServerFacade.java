
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import model.UserData;
import service.RespExp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {

    private final String baseUrl;
    private final Gson gson = new Gson();
    private String sessionToken;

    public ServerFacade(String serverAddress) {
        this.baseUrl = serverAddress;
    }

    public void resetDatabase() throws RespExp {
        sendRequest("DELETE", "/db", null, null);
    }

    public AuthData signUp(UserData user) throws RespExp {
        return sendRequest("POST", "/user", user, AuthData.class);
    }

    public AuthData signIn(UserData user) throws RespExp {
        return sendRequest("POST", "/session", user, AuthData.class);
    }

    public void signOut() throws RespExp {
        sendRequest("DELETE", "/session", null, null);
    }

    public GameData[] fetchGameList() throws RespExp {
        record GamesResponse(GameData[] games) {}
        var response = sendRequest("GET", "/game", null, GamesResponse.class);
        return response.games();
    }

    public void enterGame(JoinGameRequest gameRequest) throws RespExp {
        sendRequest("PUT", "/game", gameRequest, null);
    }

    private <T> T sendRequest(String method, String endpoint, Object payload, Class<T> responseClass) throws RespExp {
        try {
            URL targetUrl = (new URI(baseUrl + endpoint)).toURL();
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);

            attachRequestBody(payload, connection);
            connection.connect();
            checkResponseStatus(connection);
            return parseResponseBody(connection, responseClass);
        } catch (RespExp e) {
            throw e;
        } catch (Exception e) {
            throw new RespExp(500, e.getMessage());
        }
    }

    private void attachRequestBody(Object payload, HttpURLConnection connection) throws IOException {
        if (payload != null) {
            connection.setRequestProperty("Content-Type", "application/json");
            String jsonData = gson.toJson(payload);
            try (OutputStream outStream = connection.getOutputStream()) {
                outStream.write(jsonData.getBytes());
            }
        }
    }

    private void checkResponseStatus(HttpURLConnection connection) throws IOException, RespExp {
        int statusCode = connection.getResponseCode();
        if (statusCode < 200 || statusCode >= 300) {
            try (InputStream errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    throw RespExp.fromJson(errorStream);
                }
            }
            throw new RespExp(statusCode, "Unexpected response: " + statusCode);
        }
    }

    private <T> T parseResponseBody(HttpURLConnection connection, Class<T> responseClass) throws IOException {
        if (connection.getContentLength() < 0) {
            try (InputStream responseStream = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(responseStream)) {
                return responseClass != null ? gson.fromJson(reader, responseClass) : null;
            }
        }
        return null;
    }
}
