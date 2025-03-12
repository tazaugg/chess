package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;
import service.GameService;
import server.*;
import service.RespExp;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameServiceTest {

    private static GameService gameService;
    private static GameDAO gameDAO;
    private static AuthDAO authDAO;


    @BeforeAll
    static void initialize() throws DataAccessException {
        gameDAO = new MemGameDAO();
        authDAO = new MemAuthDAO();
        gameService = new GameService(gameDAO, authDAO);


    }

    @BeforeEach
    void clearBeforeTest() throws RespExp {
        gameService.clear();
    }

    @Test
    @DisplayName("Successfully Create Game")
    void testCreateGameSuccess() throws RespExp, DataAccessException {
        AuthData testAuthData = authDAO.addAuth(new AuthData("testUser", "testAuthToken"));

        int firstGameID = gameService.createGame(testAuthData.authToken(), "how");
        Assertions.assertEquals(1, gameService.listGames(testAuthData.authToken()).size());

        int secondGameID = gameService.createGame(testAuthData.authToken(), "do");
        Assertions.assertNotEquals(firstGameID, secondGameID);
    }

    @Test
    @DisplayName("Failed Game Creation due to Invalid Token")
    void testCreateGameFailure() throws RespExp {
        Assertions.assertThrows(RespExp.class, () -> gameService.createGame("invalidToken", "dumb"));
    }

    @Test
    @DisplayName("List All Games Correctly")
    void testListGamesSuccess() throws RespExp, DataAccessException {
        AuthData testAuthData = authDAO.addAuth(new AuthData("testUser", "testAuthToken"));

        int gameID1 = gameService.createGame(testAuthData.authToken(), "why");
        int gameID2 = gameService.createGame(testAuthData.authToken(), "not");
        int gameID3 = gameService.createGame(testAuthData.authToken(), "fix");


        assertEquals(3, gameService.listGames(testAuthData.authToken()).size());
    }

    @Test
    @DisplayName("Unauthorized Access to List Games")
    void testListGamesFailure() {
        Assertions.assertThrows(RespExp.class, () -> gameService.listGames("invalidToken"));
    }

    @Test
    @DisplayName("Successfully Join Game")
    void testJoinGameSuccess() throws RespExp, DataAccessException {
        AuthData testAuthData = authDAO.addAuth(new AuthData("testUser", "testAuthToken"));

        int gameID = gameService.createGame(testAuthData.authToken(), "Tell me why");

        gameService.joinGame(testAuthData.authToken(), gameID, "WHITE");

        GameData expectedGameState = new GameData(gameID, testAuthData.username(), null, "Tell me why", new ChessGame());

        assertGameDataEqual(expectedGameState, gameDAO.getGame(gameID));
    }

    @Test
    @DisplayName("Failed Attempt to Join Game")
    void testJoinGameFailure() throws RespExp, DataAccessException {
        AuthData testAuthData = authDAO.addAuth(new AuthData("testUser", "testAuthToken"));

        int gameID = gameService.createGame(testAuthData.authToken(), "Aint nothing but a heart ach");

        Assertions.assertThrows(RespExp.class, () -> gameService.joinGame("invalidToken", gameID, "WHITE"));
        Assertions.assertThrows(RespExp.class, () -> gameService.joinGame(testAuthData.authToken(), 99999, "WHITE"));
        Assertions.assertThrows(RespExp.class, () -> gameService.joinGame(testAuthData.authToken(), gameID, "INVALID"));
    }


    @Test
    @DisplayName("Successfully Clear Database")
    void testClearDatabaseSuccess() throws DataAccessException,RespExp {
        AuthData testAuthData = authDAO.addAuth(new AuthData("testUser", "testAuthToken"));

        gameService.createGame(testAuthData.authToken(), "tell me why");
        gameService.clear();
        assertEquals(0, gameService.listGames(testAuthData.authToken()).size());
    }



    public static void assertGameDataEqual(GameData expected, GameData actual){
        assertEquals(expected.gameID(), actual.gameID());
        assertEquals(expected.whiteUsername(), actual.whiteUsername());
        assertEquals(expected.blackUsername(), actual.blackUsername());
        assertEquals(expected.gameName(), actual.gameName());
        assertEquals(expected.game(), actual.game());
    }




}
