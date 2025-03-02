package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MemGameDAO implements GameDAO {
    private int gameNum = 1;

    private HashSet<GameData> gameStorage;

    public MemGameDAO() {
        gameStorage = new HashSet<>();
    }

    @Override
    public HashSet<GameData> listGames() {
        return gameStorage;
    }



    @Override
    public int createGame(GameData game) {
        game = new GameData(gameNum++, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        gameStorage.add(game);
        return game.gameID();
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameStorage.stream()
                .filter(game -> game.gameID() == gameID)
                .findFirst()
                .orElseThrow(() -> new DataAccessException("Game not found, id: " + gameID));
    }

    @Override
    public boolean gameExists(int gameID) {
        return gameStorage.stream().anyMatch(game -> game.gameID() == gameID);
    }

    @Override
    public void updateGame(GameData updatedGame) {
        gameStorage.removeIf(game -> game.gameID() == updatedGame.gameID());
        gameStorage.add(updatedGame);
    }

    @Override
    public void clear() throws DataAccessException{
        gameStorage.clear();
    }
}
