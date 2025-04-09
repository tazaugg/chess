package service;

import dataaccess.*;
import exceptions.RespExp;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserAuthService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserAuthService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData createUser(UserData userData) throws RespExp {
        try {
            UserData existingUser = userDAO.getUser(userData.username());
            if (existingUser != null) {
                throw new RespExp(403, "Error: already taken");
            }

            String hashedPassword = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
            UserData newUser = new UserData(userData.username(), hashedPassword, userData.email());
            userDAO.createUser(newUser);

            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(userData.username(), authToken);
            return authDAO.addAuth(authData);
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error: " + e.getMessage());
        }
    }

    public AuthData loginUser(UserData userData) throws RespExp {
        try {
            UserData storedUser = userDAO.getUser(userData.username());

            if (storedUser != null && BCrypt.checkpw(userData.password(), storedUser.password())) {
                String newAuthToken = UUID.randomUUID().toString();
                AuthData authData = new AuthData(userData.username(), newAuthToken);
                return authDAO.addAuth(authData);
            } else {
                throw new RespExp(401, "Error: Invalid login attempt");
            }
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error: " + e.getMessage());
        }
    }

    public void logoutUser(String authToken) throws RespExp {
        try {
            if (authDAO.getAuth(authToken) == null) {
                throw new RespExp(401, "Error: Unauthorized logout attempt");
            }
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error: " + e.getMessage());
        }
    }

    public boolean verifyToken(String authToken) throws RespExp {
        try {
            return authDAO.getAuth(authToken) != null;
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error: " + e.getMessage());
        }
    }

    public String getUsername(String authToken) throws RespExp {
        try {
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null) {
                throw new RespExp(401, "Error: Invalid token");
            }
            return authData.username();
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error: " + e.getMessage());
        }
    }

    public void clear() throws RespExp {
        try {
            userDAO.clear();
            authDAO.clear();
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error: " + e.getMessage());
        }
    }
}
