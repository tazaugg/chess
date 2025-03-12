package passoff.dataaccess;


import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.MemAuthDAO;
import dataaccess.SQLAuthDAO;
import model.AuthData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTests {

    private AuthDAO initializeAuthDAO(Class<? extends AuthDAO> daoClass) throws DataAccessException {
        AuthDAO authStorage;
        if (daoClass.equals(SQLAuthDAO.class)) {
            authStorage = new SQLAuthDAO();
        } else {
            authStorage = new MemAuthDAO();
        }
        authStorage.clear();
        return authStorage;
    }

    @ParameterizedTest
    @ValueSource(classes = {MemAuthDAO.class, SQLAuthDAO.class})
    void testCreateAuth(Class<? extends AuthDAO> daoClass) throws DataAccessException {
        AuthDAO authDAO = initializeAuthDAO(daoClass);
        var newAuth = new AuthData("", "secure_token");
        assertDoesNotThrow(() -> authDAO.addAuth(newAuth));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemAuthDAO.class, SQLAuthDAO.class})
    void testNullAuthToken(Class<? extends AuthDAO> daoClass) throws DataAccessException {
        AuthDAO authDAO = initializeAuthDAO(daoClass);
        var invalidAuth = new AuthData(null, null);
        assertThrows(DataAccessException.class, () -> authDAO.addAuth(invalidAuth));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemAuthDAO.class, SQLAuthDAO.class})
    void testRetrieveAuth(Class<? extends AuthDAO> daoClass) throws DataAccessException {
        AuthDAO authDAO = initializeAuthDAO(daoClass);
        var expectedAuth = authDAO.addAuth(new AuthData("", "user_token"));
        var retrievedAuth = authDAO.getAuth(expectedAuth.authToken());
        verifyAuthData(expectedAuth, retrievedAuth);
    }

    @ParameterizedTest
    @ValueSource(classes = {MemAuthDAO.class, SQLAuthDAO.class})
    void testAuthNotFound(Class<? extends AuthDAO> daoClass) throws DataAccessException {
        AuthDAO authDAO = initializeAuthDAO(daoClass);
        var result = authDAO.getAuth("unknown_token");
        assertNull(result);
    }
    @ParameterizedTest
    @ValueSource(classes = {MemAuthDAO.class, SQLAuthDAO.class})
    void testDeleteAuth(Class<? extends AuthDAO> daoClass) throws DataAccessException {
        AuthDAO authDAO = initializeAuthDAO(daoClass);
        List<AuthData> remainingAuths = new ArrayList<>();
        var authToRemove = authDAO.addAuth(new AuthData("", "delete_me"));
        remainingAuths.add(authDAO.addAuth(new AuthData("", "keep_one")));
        remainingAuths.add(authDAO.addAuth(new AuthData("", "keep_two")));

        Map<String, AuthData> expectedAuthMap = new HashMap<>();
        remainingAuths.forEach(auth -> expectedAuthMap.put(auth.authToken(), auth));


        assertDoesNotThrow(()->authDAO.deleteAuth(authToRemove.authToken()));

    }







    @ParameterizedTest
    @ValueSource(classes = {MemAuthDAO.class, SQLAuthDAO.class})
    void testDeleteNonExistentAuth(Class<? extends AuthDAO> daoClass) throws DataAccessException {
        AuthDAO authDAO = initializeAuthDAO(daoClass);
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth("non_existent"));
    }

    @ParameterizedTest
    @ValueSource(classes = {MemAuthDAO.class, SQLAuthDAO.class})
    void testClearAllAuths(Class<? extends AuthDAO> daoClass) throws DataAccessException {
        AuthDAO authDAO = initializeAuthDAO(daoClass);
        authDAO.addAuth(new AuthData("", "user_a"));
        authDAO.addAuth(new AuthData("", "user_b"));
        authDAO.addAuth(new AuthData("", "user_c"));
        assertDoesNotThrow(()->authDAO.clear());
    }



    private static void verifyAuthData(AuthData expected, AuthData actual) {
        assertEquals(expected.username(), actual.username());
        assertEquals(expected.authToken(), actual.authToken());
    }


}

