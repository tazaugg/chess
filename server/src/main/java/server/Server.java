package server;

import dataaccess.*;
import spark.*;
import service.*;

public class Server {

    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    UserAuthService userAuthService;
    GameService gameService;

    UserAuthHandler userAuthHandler;
    GameHandler gameHandler;

    public Server() {

        userDAO = new MemUserDAO();
        authDAO = new MemAuthDAO();
        gameDAO = new MemGameDAO();

        try{
            userDAO = new SQLUserDAO();
            authDAO = new SQLAuthDAO();
            gameDAO = new SQLGameDAO();
        }
        catch(DataAccessException e){
            userDAO = new MemUserDAO();
            authDAO = new MemAuthDAO();
            gameDAO = new MemGameDAO();
        }

        userAuthService = new UserAuthService(userDAO, authDAO);
        gameService = new GameService(gameDAO,authDAO);

        userAuthHandler = new UserAuthHandler(userAuthService);
        gameHandler = new GameHandler(gameService);

    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", userAuthHandler::register);
        Spark.delete("/db", this::clear);

        Spark.post("/session", userAuthHandler::login);
        Spark.delete("/session", userAuthHandler::logout);

        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);

        Spark.exception(RespExp.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(RespExp exp, Request req, Response res) {
        res.status(exp.statusCode());
        res.body(exp.toJson());
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
    private Object clear(Request req, Response resp) throws RespExp {
        userAuthService.clear();
        gameService.clear();

        resp.status(200);
        return "";



    }
}
