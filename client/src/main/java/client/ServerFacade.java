package client;

import com.google.gson.Gson;
import exceptions.RespExp;
import model.AuthData;
import model.GameData;

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

    /** Login an existing user*/
    public AuthData login(String username, String password) throws RespExp {
        String path = "/session";
        Map<String, String> requestBody = Map.of(
                "username", username,
                "password", password
        );
        return makeRequest("POST", path, requestBody, AuthData.class, null);
    }

    /** Logout current user */
    public void logout(String authToken) throws RespExp {
        String path = "/session";
        makeRequest("DELETE", path, null, null, authToken);
    }

    public int createGame(String gameName, String authToken) throws RespExp {
        String path = "/game";
        Map<String, String> requestBody = Map.of("gameName", gameName);
        record GameID(int gameID) { };
        GameID gameID = makeRequest("POST", path, requestBody, GameID.class, authToken);
        return gameID.gameID();
    }

    public GameData[] listGames(String authToken) throws RespExp {
        String path = "/game";
        record GamesList(GameData[] games) {};
        GamesList games = makeRequest("GET", path, null, GamesList.class, authToken);
        return games.games();
    }

    public void joinGame(int gameID, String playerColor, String authToken) throws RespExp {
        String path = "/game";
        Map<String, String> requestBody = Map.of(
                "playerColor", playerColor,
                "gameID", String.format("%d",gameID)
        );
        makeRequest("PUT", path, requestBody, null, authToken);
    }



    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws RespExp {
        try {
            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if (authToken != null) {
                http.setRequestProperty("Authorization", authToken);
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
