package client;

import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade; // Ensure the correct import path
import service.RespExp;
import model.AuthData;
import model.UserData;

public class ServerFacadeTests {

    private static Server testServer;
    private static ServerFacade facade;

    @BeforeAll
    public static void startServer() {
        testServer = new Server();
        int port = testServer.run(0);
        System.out.println("Server initialized on port: " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void wipeDatabase() throws RespExp {
        facade.resetDatabase();
    }

    @AfterAll
    public static void stopServer() {
        testServer.stop();
    }

    private String registerUser() throws RespExp {
        UserData newUser = new UserData("sampleUser", "securePass", "user@example.com");
        AuthData auth = facade.createUser(newUser.username(), newUser.password(), newUser.email());
        return auth.authToken();
    }

    @Test
    public void basicTest() {
        Assertions.assertTrue(true);
    }
}
