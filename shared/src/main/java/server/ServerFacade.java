package server;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.RespExp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class ServerFacade {

    private final String baseUrl;

    public ServerFacade(String url) {
        baseUrl = url;
    }

    public void resetDatabase() throws RespExp {
        var endpoint = "/db";
        this.sendRequest("DELETE", endpoint, null, null, null);
    }

    public AuthData createUser(String username, String password, String email) throws RespExp {
        var endpoint = "/user";
        var payload = Map.of("username", username, "password", password, "email", email);
        return this.sendRequest("POST", endpoint, payload, AuthData.class, null);
    }

    public AuthData authenticate(String username, String password) throws RespExp {
        var endpoint = "/session";
        var payload = Map.of("username", username, "password", password);
        return this.sendRequest("POST", endpoint, payload, AuthData.class, null);
    }

    public void endSession(String token) throws RespExp {
        var endpoint = "/session";
        this.sendRequest("DELETE", endpoint, null, null, token);
    }

    public GameData[] retrieveGames() throws RespExp {
        var endpoint = "/game";
        record GameListResponse(GameData[] games) {}
        var response = this.sendRequest("GET", endpoint, null, GameListResponse.class, null);
        return response.games();
    }

    public int initializeGame(String gameTitle, String token) throws RespExp {
        var endpoint = "/game";
        record GameResponse(int gameID) {}
        var payload = Map.of("name", gameTitle);
        var response = this.sendRequest("POST", endpoint, payload, GameResponse.class, token);
        return response.gameID();
    }

    public void participateInGame(String color, int gameId, String token) throws RespExp {
        var payload = Map.of("playerColor", color, "gameID", gameId);
        var endpoint = "/game";
        this.sendRequest("PUT", endpoint, payload, null, token);
    }

    private <T> T sendRequest(String method, String endpoint, Object payload, Class<T> responseType, String token) throws RespExp {
        try {
            URL url = (new URI(baseUrl + endpoint)).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);

            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }

            writePayload(payload, connection);
            connection.connect();
            handleErrorResponse(connection);
            return parseResponse(connection, responseType);

        } catch (RespExp e) {
            throw e;
        } catch (Exception e) {
            throw new RespExp(500, "Unexpected error: " + e.getMessage());
        }
    }

    private static void writePayload(Object payload, HttpURLConnection connection) throws IOException {
        if (payload != null) {
            connection.addRequestProperty("Content-Type", "application/json");
            String json = new Gson().toJson(payload);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(json.getBytes());
            }
        }
    }

    private void handleErrorResponse(HttpURLConnection connection) throws IOException, RespExp {
        int statusCode = connection.getResponseCode();
        if (!isSuccess(statusCode)) {
            try (InputStream errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    throw RespExp.fromJson(errorStream);
                }
            }
            throw new RespExp(statusCode, "HTTP error: " + statusCode);
        }
    }

    private static <T> T parseResponse(HttpURLConnection connection, Class<T> responseType) throws IOException {
        T result = null;
        if (connection.getContentLength() < 0) {
            try (InputStream responseStream = connection.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(responseStream);
                if (responseType != null) {
                    result = new Gson().fromJson(reader, responseType);
                }
            }
        }
        return result;
    }

    private boolean isSuccess(int statusCode) {
        return statusCode / 100 == 2;
    }
}
