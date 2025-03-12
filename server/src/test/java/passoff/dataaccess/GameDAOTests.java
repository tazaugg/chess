package passoff.dataaccess;


import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemGameDAO;
import dataaccess.SQLGameDAO;
import model.GameData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTests {

    private GameDAO initializeGameDAO(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO database;
        if (daoType.equals(SQLGameDAO.class)) {
            database = new SQLGameDAO();
        } else {
            database = new MemGameDAO();
        }
        database.clear();
        return database;
    }

    @ParameterizedTest
    @ValueSource(classes = {MemGameDAO.class, SQLGameDAO.class})
    void testCreateGame(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO gameDAO = initializeGameDAO(daoType);
        GameData gameData = new GameData(0, null, null, "game1", new ChessGame());
        assertDoesNotThrow(() -> gameDAO.createGame(gameData));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemGameDAO.class, SQLGameDAO.class})
    void testPreventDuplicateGames(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO gameDAO = initializeGameDAO(daoType);
        GameData gameData = new GameData(0, null, null, "game1", new ChessGame());
        gameDAO.createGame(gameData);
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(gameData));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemGameDAO.class, SQLGameDAO.class})
    void testRetrieveGame(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO gameDAO = initializeGameDAO(daoType);
        GameData expectedGame = gameDAO.createGame(new GameData(0, null, null, "game1", new ChessGame()));
        GameData retrievedGame = gameDAO.getGame(expectedGame.gameID());
        verifyGameData(expectedGame, retrievedGame);
    }

    @ParameterizedTest
    @ValueSource(classes = {MemGameDAO.class, SQLGameDAO.class})
    void testMissingGameReturnsNull(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO gameDAO = initializeGameDAO(daoType);
        assertNull(gameDAO.getGame(999));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemGameDAO.class, SQLGameDAO.class})
    void testListGames(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO gameDAO = initializeGameDAO(daoType);
        List<GameData> expectedGames = new ArrayList<>();
        expectedGames.add(gameDAO.createGame(new GameData(0, null, null, "game1", new ChessGame())));
        expectedGames.add(gameDAO.createGame(new GameData(0, null, null, "game2", new ChessGame())));
        expectedGames.add(gameDAO.createGame(new GameData(0, null, null, "game3", new ChessGame())));
        assertCollectionsMatch(expectedGames, gameDAO.listGames());
    }

    @ParameterizedTest
    @ValueSource(classes = {MemGameDAO.class, SQLGameDAO.class})
    void testEmptyGameList(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO gameDAO = initializeGameDAO(daoType);
        assertEquals(0, gameDAO.listGames().size());
    }



    @ParameterizedTest
    @ValueSource(classes = {MemGameDAO.class, SQLGameDAO.class})
    void testClearGames(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO gameDAO = initializeGameDAO(daoType);
        gameDAO.createGame(new GameData(0, null, null, "game1", new ChessGame()));
        gameDAO.createGame(new GameData(0, null, null, "game2", new ChessGame()));
        gameDAO.createGame(new GameData(0, null, null, "game3", new ChessGame()));

        gameDAO.clear();
        assertEquals(0, gameDAO.listGames().size());
    }
    @ParameterizedTest
    @ValueSource(classes = {MemGameDAO.class, SQLGameDAO.class})
    void testUpdateGame(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO gameDAO = initializeGameDAO(daoType);
        GameData gameData = gameDAO.createGame(new GameData(0, null, null, "game1", new ChessGame()));

        String newWhitePlayer = "player1";
        String newBlackPlayer = "player2";
        ChessMove move = new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1), null);
        gameData = gameDAO.getGame(gameData.gameID());

        GameData expectedUpdate1 = gameDAO.updateGame(
                new GameData(gameData.gameID(), newWhitePlayer, gameData.blackUsername(), gameData.gameName(), gameData.game()));
        assertEquals(expectedUpdate1, gameDAO.getGame(gameData.gameID()));

        gameData = gameDAO.getGame(gameData.gameID());
        GameData expectedUpdate2 = gameDAO.updateGame(
                new GameData(gameData.gameID(), gameData.whiteUsername(), newBlackPlayer, gameData.gameName(), gameData.game()));
        assertEquals(expectedUpdate2, gameDAO.getGame(gameData.gameID()));

        gameData = gameDAO.getGame(gameData.gameID());
        ChessGame updatedGame = gameData.game();
        assertDoesNotThrow(() -> updatedGame.makeMove(move));

        GameData finalUpdate = gameDAO.updateGame(
                new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), updatedGame));
        assertEquals(finalUpdate, gameDAO.getGame(gameData.gameID()));
    }



    @ParameterizedTest
    @ValueSource(classes = {MemGameDAO.class, SQLGameDAO.class})
    void testUpdatingNonexistentGame(Class<? extends GameDAO> daoType) throws DataAccessException {
        GameDAO gameDAO = initializeGameDAO(daoType);
        GameData gameData = new GameData(0, null, null, "game1", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(gameData));
    }

    public static void verifyGameData(GameData expected, GameData actual) {
        assertEquals(expected.gameID(), actual.gameID());
        assertEquals(expected.blackUsername(), actual.blackUsername());
        assertEquals(expected.whiteUsername(), actual.whiteUsername());
        assertEquals(expected.gameName(), actual.gameName());
        assertEquals(expected.game(), actual.game());
    }

    public static void assertCollectionsMatch(Collection<GameData> expected, Collection<GameData> actual) {
        GameData[] expectedArr = expected.toArray(new GameData[0]);
        GameData[] actualArr = actual.toArray(new GameData[0]);
        assertEquals(expectedArr.length, actualArr.length);
        for (int i = 0; i < expectedArr.length; i++) {
            verifyGameData(expectedArr[i], actualArr[i]);
        }
    }
}

