package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.*;
import java.util.stream.Collectors;

public class MemGameDAO implements GameDAO {
    private int nextId = 1;

    private Map<Integer, GameData> gameStorage;

    public MemGameDAO() {
        gameStorage = new HashMap<>();
    }

    @Override
    public Collection<GameData> listGames() {
        return gameStorage.values();
    }



    @Override
    public GameData createGame(GameData game) {
        game = new GameData(nextId++, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        gameStorage.put(game.gameID(), game);
        return game;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameStorage.get(gameID);
    }

    @Override
    public boolean gameExists(int gameID) {
        return gameStorage.containsKey(gameID);
    }

    @Override
    public void updateGame(GameData updatedGame) {
        gameStorage.put(updatedGame.gameID(), updatedGame);
    }

    @Override
    public void clear() throws DataAccessException{
        gameStorage.values().clear();
        gameStorage.clear();
    }
}
