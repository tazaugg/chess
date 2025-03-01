package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.UserAuthService;
import spark.Request;
import spark.Response;

public class UserAuthHandler {

    private final UserAuthService authService;

    public UserAuthHandler(UserAuthService authService) {
        this.authService = authService;
    }

    public Object register(Request req, Response resp) {
        try {
            // Parse incoming JSON request
            UserData userData = new Gson().fromJson(req.body(), UserData.class);

            // Attempt to create user and return auth token
            AuthData authData = authService.createUser(userData);
            resp.status(200);
            return new Gson().toJson(authData);

        } catch (DataAccessException e) {
            resp.status(403);
            return "{ \"message\": \"Error: username already taken\" }";

        } catch (JsonSyntaxException e) {
            resp.status(400);
            return "{ \"message\": \"Error: invalid request format\" }";

        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: %s\" }".formatted(e.getMessage());
        }
    }

    public Object login(Request req, Response resp) {
        try {
            UserData credentials = new Gson().fromJson(req.body(), UserData.class);
            AuthData authToken = authService.loginUser(credentials);
            resp.status(200);
            return new Gson().toJson(authToken);
        } catch (DataAccessException e) {
            resp.status(401);
            return "{ \"message\": \"Error: invalid credentials\" }";
        } catch (JsonSyntaxException e) {
            resp.status(400);
            return "{ \"message\": \"Error: bad request format\" }";
        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: unexpected server issue\" }";
        }
    }

    public Object logout(Request req, Response resp) {
        try {
            String token = req.headers("authorization");
            authService.logoutUser(token);
            resp.status(200);
            return "{}";
        } catch (DataAccessException e) {
            resp.status(401);
            return "{ \"message\": \"Error: unauthorized request\" }";
        } catch (Exception e) {
            resp.status(500);
            return "{ \"message\": \"Error: unexpected server issue\" }";
        }
    }
}
