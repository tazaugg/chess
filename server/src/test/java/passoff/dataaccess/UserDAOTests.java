package passoff.dataaccess;


import dataaccess.DataAccessException;
import dataaccess.MemUserDAO;
import dataaccess.SQLUserDAO;
import dataaccess.UserDAO;
import model.UserData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTests {

    private UserDAO getUserDAO(Class<? extends UserDAO> userDatabaseClass) throws DataAccessException {
        UserDAO db = userDatabaseClass.equals(SQLUserDAO.class) ? new SQLUserDAO() : new MemUserDAO();
        db.clear();
        return db;
    }

    @ParameterizedTest
    @ValueSource(classes = {MemUserDAO.class, SQLUserDAO.class})
    void createUser(Class<? extends UserDAO> dbClass) throws DataAccessException {
        UserDAO userDAO = getUserDAO(dbClass);
        var userData = new UserData("bill_nye_science", "12345", "billnye@scienceguy.com");
        assertDoesNotThrow(() -> userDAO.createUser(userData));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemUserDAO.class, SQLUserDAO.class})
    void noDuplicate(Class<? extends UserDAO> dbClass) throws DataAccessException {
        UserDAO userDAO = getUserDAO(dbClass);
        var userData = new UserData("bill_nye_science", "12345", "billnye@scienceguy.com");
        userDAO.createUser(userData);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(userData));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemUserDAO.class, SQLUserDAO.class})
    void getUser(Class<? extends UserDAO> dbClass) throws DataAccessException {
        UserDAO userDAO = getUserDAO(dbClass);
        var expected = new UserData("bill_nye_science", "12345", "billnye@scienceguy.com");
        userDAO.createUser(expected);
        var actual = userDAO.getUser(expected.username());
        assertUserDataEqual(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(classes = {MemUserDAO.class, SQLUserDAO.class})
    void noUser(Class<? extends UserDAO> dbClass) throws DataAccessException {
        UserDAO userDAO = getUserDAO(dbClass);
        assertNull(userDAO.getUser("bill_nye_science"));
    }


    @ParameterizedTest
    @ValueSource(classes = {MemUserDAO.class, SQLUserDAO.class})
    void clear(Class<? extends UserDAO> dbClass) throws DataAccessException {
        UserDAO userDAO = getUserDAO(dbClass);
        var expected = new UserData("bill_nye_science", "12345", "billnye@scienceguy.com");
        userDAO.createUser(expected);
        var actual = userDAO.getUser(expected.username());
        assertUserDataEqual(expected, actual);
        userDAO.clear();
        assertNull(userDAO.getUser("bill_nye_science"));
    }









    private static void assertUserDataEqual(UserData expected, UserData actual) {
        assertEquals(expected.username(), actual.username());
        assertEquals(expected.password(), actual.password());
        assertEquals(expected.email(), actual.email());
    }


}
