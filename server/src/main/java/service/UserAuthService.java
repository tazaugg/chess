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

    public AuthData createUser(UserData userData) throws RespExp {
        try{
            UserData gotUser = userDAO.getUser(userData.username());
            if(gotUser != null){
                throw new RespExp(403, "Error: already taken");
            }
            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(userData.username(), authToken);

            userDAO.createUser(userData);


            return authDAO.addAuth(authData);

        }
        catch(DataAccessException e){
            throw new RespExp(500, "Error" + e.getMessage());
        }

    }

    public AuthData loginUser(UserData userData) throws RespExp {
       try{
           UserData gotUser = userDAO.getUser(userData.username());

           if (gotUser != null && gotUser.password().equals(userData.password())) {
               String newAuthToken = UUID.randomUUID().toString();
               AuthData authRecord = new AuthData(userData.username(), newAuthToken);
               return  authDAO.addAuth(authRecord);
           }
           else{
               throw new RespExp(401, "Error: Invalid login attempt");
           }

       }
       catch(DataAccessException e){
           throw new RespExp(500, e.getMessage());
       }
    }

    public void logoutUser(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: Unauthorized logout attempt");
        }
        authDAO.deleteAuth(authToken);
    }



    public void clear() throws RespExp {
        try {
            userDAO.clear();
            authDAO.clear();
        }
        catch(DataAccessException e){
            throw new RespExp(500, "Error: " + e.getMessage());
        }

    }
}
