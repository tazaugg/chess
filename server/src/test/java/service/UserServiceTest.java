package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.*;

public class UserServiceTest {

    static UserAuthService userService;
    static UserDAO userDAO;
    static AuthDAO authDAO;

    static UserData testUser;


    @BeforeAll
    static void setUpClass() {
        userDAO = new MemUserDAO();
        authDAO = new MemAuthDAO();
        userService = new UserAuthService(userDAO, authDAO);
    }

    @BeforeEach
    void setUpTest() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();

        testUser = new UserData("Username", "password", "email");
    }

    @Test
    @DisplayName("Successfully Create a User")
    void testCreateUserSuccess() throws RespExp, DataAccessException {
        AuthData generatedAuth = userService.createUser(testUser);
        Assertions.assertEquals(authDAO.getAuth(generatedAuth.authToken()), generatedAuth);
    }

    @Test
    @DisplayName("Attempt to Create a Duplicate User")
    void testCreateUserFailure() throws RespExp {
        userService.createUser(testUser);
        Assertions.assertThrows(RespExp.class, () -> userService.createUser(testUser));
    }

    @Test
    @DisplayName("Successful Login of User")
    void testLoginUserSuccess() throws RespExp, DataAccessException {
        userService.createUser(testUser);
        AuthData loggedInAuth = userService.loginUser(testUser);
        Assertions.assertEquals(authDAO.getAuth(loggedInAuth.authToken()), loggedInAuth);
    }

    @Test
    @DisplayName("Login Failure with Incorrect Credentials")
    void testLoginUserFailure() throws RespExp {
        Assertions.assertThrows(UnauthExcept.class, () -> userService.loginUser(testUser));

        userService.createUser(testUser);
        UserData incorrectPassUser = new UserData(testUser.username(), "wrongPass", testUser.email());
        Assertions.assertThrows(UnauthExcept.class, () -> userService.loginUser(incorrectPassUser));
    }

    @Test
    @DisplayName("Successful Logout of User")
    void testLogoutUserSuccess() throws RespExp, DataAccessException {
        AuthData auth = userService.createUser(testUser);
        userService.logoutUser(auth.authToken());
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.getAuth(auth.authToken()));
    }

    @Test
    @DisplayName("Logout Failure with Invalid Auth Token")
    void testLogoutUserFailure() throws RespExp {
        AuthData auth = userService.createUser(testUser);
        Assertions.assertThrows(UnauthExcept.class, () -> userService.logoutUser("invalidAuthToken"));
    }

    @Test
    @DisplayName("Clear Database Successfully")
    void testClearDbSuccess() throws RespExp {
        AuthData auth = userService.createUser(testUser);
        userService.clear();
        Assertions.assertThrows(DataAccessException.class, () -> userDAO.getUser(testUser.username()));
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.getAuth(auth.authToken()));
    }

    @Test
    @DisplayName("Clear Database with No Errors")
    void testClearDbFailure() throws RespExp {
        Assertions.assertDoesNotThrow(() -> userService.clear());
    }

}
