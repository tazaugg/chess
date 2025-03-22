package client;

import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import service.RespExp;

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
}
