package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthExcept;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.Set;

public class GameHandler {

    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object listGames(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");
            Set<GameData> availableGames = gameService.listGames(authToken);
            resp.status(200);
            return new Gson().toJson(new GameListResponse(availableGames));
        } catch (DataAccessException e) {
            resp.status(401);
            return errorResponse("Error: unauthorized");
        } catch (Exception e) {
            resp.status(500);
            return errorResponse("Error: " + e.getMessage());
        }
    }

    public Object createGame(Request req, Response resp) {
        if (!req.body().contains("\"gameName\":")) {
            resp.status(400);
            return errorResponse("Error: bad request");
        }

        try {
            String authToken = req.headers("authorization");
            int newGameID = gameService.createGame(authToken);
            resp.status(200);
            return new Gson().toJson(new GameIDResponse(newGameID));
        } catch (DataAccessException e) {
            resp.status(401);
            return errorResponse("Error: unauthorized");
        } catch (Exception e) {
            resp.status(500);
            return errorResponse("Error: " + e.getMessage());
        }
    }

    public Object joinGame(Request req, Response resp) {
        if (!req.body().contains("\"gameID\":")) {
            resp.status(400);
            return errorResponse("Error: bad request");
        }

        try {
            String authToken = req.headers("authorization");
            JoinRequest joinRequest = new Gson().fromJson(req.body(), JoinRequest.class);
            int joinResult = gameService.joinGame(authToken, joinRequest.gameID(), joinRequest.playerColor());

            return handleJoinGameResponse(joinResult, resp);

        } catch (DataAccessException e) {
            resp.status(400);
            return errorResponse("Error: bad request");
        } catch (UnauthExcept e) {
            resp.status(401);
            return errorResponse("Error: unauthorized");
        } catch (Exception e) {
            resp.status(500);
            return errorResponse("Error: " + e.getMessage());
        }
    }

    private Object handleJoinGameResponse(int result, Response resp) {
        switch (result) {
            case 0:
                resp.status(200);
                return "{}";
            case 1:
                resp.status(400);
                return errorResponse("Error: bad request");
            case 2:
                resp.status(403);
                return errorResponse("Error: already taken");
            default:
                resp.status(500);
                return errorResponse("Error: unexpected response");
        }
    }

    private String errorResponse(String message) {
        return "{ \"message\": \"" + message + "\" }";
    }

    private record GameListResponse(Set<GameData> games) {}

    private record GameIDResponse(int gameID) {}

    private record JoinRequest(String playerColor, int gameID) {}

}
