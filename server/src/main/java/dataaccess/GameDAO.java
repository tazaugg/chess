package dataaccess;
import model.GameData;
import chess.ChessGame;

import java.util.Collection;
import java.util.HashSet;

public interface GameDAO {
    Collection<GameData> listGames() throws DataAccessException;
    GameData createGame(GameData game)throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    boolean gameExists(int gameID)throws DataAccessException;

    GameData updateGame(GameData game)throws DataAccessException;

    void clear() throws DataAccessException;
}
