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
        // TODO: Implement login logic
        return null;
    }
}
