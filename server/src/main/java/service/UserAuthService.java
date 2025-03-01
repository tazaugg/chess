package service;
import dataaccess.*;
import model.*;

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


    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}
