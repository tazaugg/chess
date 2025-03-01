package service;
import dataaccess.*;
import model.*;
import spark.Response;

import java.util.UUID;

public class UserAuthService {
    UserDAO userDAO;
    AuthDAO authDAO;

    public UserAuthService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData createUser(UserData userData) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(userData.username(), authToken);

        userDAO.createUser(userData);
        authDAO.addAuth(authData);

        return authData;
    }

    public AuthData loginUser(UserData userData) throws DataAccessException {
        if (!userDAO.authUser(userData.username(), userData.password())) {
            throw new DataAccessException("Invalid login attempt");
        }
        String newAuthToken = UUID.randomUUID().toString();
        AuthData authRecord = new AuthData(userData.username(), newAuthToken);
        authDAO.addAuth(authRecord);
        return authRecord;
    }

    public void logoutUser(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Unauthorized logout attempt");
        }
        authDAO.deleteAuth(authToken);
    }



    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}
