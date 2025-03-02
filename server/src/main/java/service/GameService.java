package service;

import dataaccess.*;
import model.*;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public HashSet<GameData> listGames(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Unauthorized access");
        }
        return gameDAO.listGames(authToken);
    }

    public int createGame(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Invalid authentication");
        }

        int newGameID;
        do {
            newGameID = ThreadLocalRandom.current().nextInt(1, 10000);
        } while (gameDAO.gameExists(newGameID));

        GameData newGame = new GameData(newGameID, null, null, null, null);
        gameDAO.createGame(newGame);
        return newGameID;
    }

    public int joinGame(String authToken, int gameID, String color) throws UnauthExcept, DataAccessException {
        AuthData userAuth = authDAO.getAuth(authToken);
        if (userAuth == null) {
            throw new UnauthExcept("Invalid token provided");
        }

        if (!gameDAO.gameExists(gameID)) {
            return 1; // Game does not exist
        }

        GameData currentGame = gameDAO.getGame(gameID);
        String whitePlayer = currentGame.whiteUsername();
        String blackPlayer = currentGame.blackUsername();

        if ("WHITE".equals(color)) {
            if (whitePlayer != null) return 2; // Already taken
            whitePlayer = userAuth.username();
        } else if ("BLACK".equals(color)) {
            if (blackPlayer != null) return 2; // Already taken
            blackPlayer = userAuth.username();
        } else if (color != null) {
            return 1; // Invalid request
        }

        gameDAO.updateGame(new GameData(gameID, whitePlayer, blackPlayer, currentGame.gameName(), currentGame.game()));
        return 0;
    }

    public void clear() throws RespExp {
        try{
            gameDAO.clear();
        }
        catch(DataAccessException e){
            throw new RespExp(500, "Error" + e.getMessage());
        }
    }
}
