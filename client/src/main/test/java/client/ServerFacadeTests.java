package client;

import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;
import exceptions.RespExp;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void setupServer() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Server started on port: " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    @AfterEach
    public void resetDatabase() throws RespExp {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    private String createTestUser() throws RespExp {
        AuthData auth = facade.register("testUser", "testPass", "test@example.com");
        return auth.authToken();
    }

    @Test
    public void testUserRegistration() throws RespExp {
        Assertions.assertDoesNotThrow(this::createTestUser);
    }

    @Test
    public void testRegisterDuplicate() throws RespExp{
        createTestUser();
        Assertions.assertThrows(RespExp.class, this::createTestUser);
    }

    @Test
    public void testUserLogin() throws RespExp {
        createTestUser();
        Assertions.assertDoesNotThrow(() -> facade.login("testUser", "testPass"));
    }

    @Test
    public void testLoginNegative() throws RespExp {
        Assertions.assertThrows(RespExp.class, () -> facade.login("testUser", "testPass"));
        createTestUser();
        Assertions.assertThrows(RespExp.class, () -> facade.login("testUser", "badPass"));
    }

    @Test
    public void testUserLogout() throws RespExp {
        var auth = createTestUser();
        Assertions.assertDoesNotThrow(() -> facade.logout(auth));
    }

    @Test
    public void logoutNegative() throws RespExp {
        var auth = createTestUser();
        Assertions.assertThrows(RespExp.class, () -> facade.logout(auth + "bad"));
    }

    public int createGame(String authToken) throws RespExp {
        return facade.createGame("A_game", authToken);
    }

    @Test
    public void testGameCreation() throws RespExp {
        var auth = createTestUser();
        Assertions.assertDoesNotThrow(() -> createGame(auth));
    }

    @Test
    public void testGameCreationNegative() throws RespExp {
        var auth = createTestUser();
        Assertions.assertThrows(RespExp.class, () -> createGame(auth + "bad"));
        createGame(auth);
        Assertions.assertThrows(RespExp.class, () -> createGame(auth));
    }

    @Test
    public void testListGames() throws RespExp {
        var auth = createTestUser();
        createGame(auth);
        Assertions.assertDoesNotThrow(() -> facade.listGames(auth));
        Assertions.assertEquals(1, facade.listGames(auth).length);
    }

    @Test
    public void testListGamesNegative() throws RespExp {
        var auth = createTestUser();
        Assertions.assertThrows(RespExp.class, () -> facade.listGames(auth + "bad"));
    }

    @Test
    public void testJoinGamePositive() throws RespExp {
        var auth = createTestUser();
        var gameID = createGame(auth);
        Assertions.assertDoesNotThrow(() -> facade.joinGame(gameID, "white", auth));

        Assertions.assertDoesNotThrow(() -> facade.joinGame(gameID, "black", auth));
    }

    @Test
    public void testJoinGameNegative() throws RespExp {
        var auth = createTestUser();
        var gameID = createGame(auth);
        Assertions.assertThrows(RespExp.class, () -> facade.joinGame(gameID, "white", auth+"bad"));
        facade.joinGame(gameID, "white", auth);
        Assertions.assertThrows(RespExp.class, () -> facade.joinGame(gameID, "white", auth));
    }


}
