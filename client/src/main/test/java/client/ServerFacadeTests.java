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

}
