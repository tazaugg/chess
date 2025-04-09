package server;

import dataaccess.*;
import exceptions.RespExp;
import model.AuthData;
import server.websocket.WebSocketHandler;
import service.*;
import spark.*;

import static spark.Spark.halt;

public class Server {

    private final ClearService clearService;
    private final UserAuthService userAuthService;
    private final GameService gameService;
    private final UserAuthHandler userAuthHandler;
    private final GameHandler gameHandler;
    private final WebSocketHandler webSocketHandler;

    public Server() {
        UserDAO userDAO = new MemUserDAO();
        AuthDAO authDAO = new MemAuthDAO();
        GameDAO gameDAO = new MemGameDAO();

        try {
            userDAO = new SQLUserDAO();
            authDAO = new SQLAuthDAO();
            gameDAO = new SQLGameDAO();
        } catch (DataAccessException e) {
            userDAO = new MemUserDAO();
            authDAO = new MemAuthDAO();
            gameDAO = new MemGameDAO();
        }

        userAuthService = new UserAuthService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(authDAO, userDAO, gameDAO);

        userAuthHandler = new UserAuthHandler(userAuthService);
        gameHandler = new GameHandler(gameService);
        webSocketHandler = new WebSocketHandler(gameService, userAuthService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", webSocketHandler);

        Spark.before("/session", this::authFilter);
        Spark.before("/game", this::authFilter);

        Spark.post("/user", userAuthHandler::register);
        Spark.post("/session", userAuthHandler::login);
        Spark.delete("/session", userAuthHandler::logout);

        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);
        Spark.delete("/db", this::clear);

        Spark.exception(RespExp.class, this::exceptionHandler);

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    private void authFilter(Request req, Response res) throws RespExp {
        String path = req.pathInfo();
        String method = req.requestMethod().toLowerCase();
        if(!path.equals("/session") || !method.equals("post")) {
            String authHeader = req.headers("authorization");
            if (authHeader == null || !userAuthService.verifyToken(authHeader)) {
                throw new RespExp(401, "Error: unauthorized");
            }
        }
    }

    private void exceptionHandler(RespExp ex, Request req, Response res) {
        res.status(ex.statusCode());
        res.body(ex.toJson());
    }

    private Object clear(Request req, Response res) throws RespExp {
        clearService.clear();
        res.status(200);
        return "";
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
