package server;

import com.google.gson.Gson;
import model.GameData;
import service.GameService;
import exceptions.RespExp;
import spark.Request;
import spark.Response;

import java.util.Collection;

public class GameHandler {

    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object listGames(Request req, Response resp) throws RespExp {
        String authToken = req.headers("authorization");
        GameListResponse availableGames = new GameListResponse(gameService.listGames(authToken));
        resp.status(200);
        resp.body(new Gson().toJson(availableGames));
        return resp.body();

    }

    public Object createGame(Request req, Response resp) throws RespExp{
        if (!req.body().contains("\"gameName\":")) {
            resp.status(400);
            return errorResponse("Error: bad request");
        }
        String authToken = req.headers("authorization");
        GameData gameData = new Gson().fromJson(req.body(), GameData.class);

        int newGameID = gameService.createGame(authToken, gameData.gameName());
        resp.status(200);
        return new Gson().toJson(new GameIDResponse(newGameID));

    }

    public Object joinGame(Request req, Response resp) throws RespExp {
        if (!req.body().contains("\"gameID\":")) {
            resp.status(400);
            throw new RespExp(400,"Error: bad request");
        }
        String authToken = req.headers("authorization");
        JoinRequest joinRequest = new Gson().fromJson(req.body(), JoinRequest.class);
        int joinResult = gameService.joinGame(authToken, joinRequest.gameID(), joinRequest.playerColor());

        return handleJoinGameResponse(joinResult, resp);

    }

    private Object handleJoinGameResponse(int result, Response resp) throws RespExp {
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

    private record GameListResponse(Collection<GameData> games) {}

    private record GameIDResponse(int gameID) {}

    private record JoinRequest(String playerColor, int gameID) {}

}
