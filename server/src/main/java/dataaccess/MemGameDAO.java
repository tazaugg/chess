package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MemGameDAO implements GameDAO {

    private HashSet<GameData> gameStorage;

    public MemGameDAO() {
        gameStorage = new HashSet<>();
    }

    @Override
    public HashSet<GameData> listGames(String username) {
        return gameStorage.stream()
                .filter(game -> username.equals(game.whiteUsername()) || username.equals(game.blackUsername()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public void createGame(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        gameStorage.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
    }

    @Override
    public void createGame(GameData game) {
        gameStorage.add(game);
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
    public void clear() {
        gameStorage.clear();
    }
}
