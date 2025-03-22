package server;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import exceptions.RespExp;
import service.UserAuthService;
import spark.Request;
import spark.Response;

public class UserAuthHandler {

    private final UserAuthService authService;

    public UserAuthHandler(UserAuthService authService) {
        this.authService = authService;
    }

    public Object register(Request req, Response resp) throws RespExp{
        UserData userData = new Gson().fromJson(req.body(), UserData.class);
        if(userData.username() == null || userData.password() == null || userData.email() == null){
            resp.status(400);
            throw new RespExp(400, "Error: bad request");
        }
        else{
            AuthData authData = authService.createUser(userData);
            resp.status(200);
            resp.body(new Gson().toJson(authData));

        }
        return resp.body();
    }

    public Object login(Request req, Response resp) throws RespExp {
        UserData credentials = new Gson().fromJson(req.body(), UserData.class);
        if(credentials.username() == null || credentials.password() == null) {
            resp.status(401);
            throw new RespExp(401, "Error: unauthorized");
        }
        else{
            AuthData authToken = authService.loginUser(credentials);
            resp.status(200);
            resp.body(new Gson().toJson(authToken));
        }
        return resp.body();
    }

    public Object logout(Request req, Response resp) throws RespExp {
        String token = req.headers("authorization");
        authService.logoutUser(token);
        resp.status(200);
        return "{}";

    }
}
