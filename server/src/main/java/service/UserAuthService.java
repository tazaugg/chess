package service;
import dataaccess.*;
import exceptions.RespExp;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

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
            String hash = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
            userData = new UserData(userData.username(), hash, authToken);
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

           if (gotUser != null && BCrypt.checkpw(userData.password(), gotUser.password())) {
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

    public void logoutUser(String authToken) throws RespExp{
        try{
            if (authDAO.getAuth(authToken) == null) {
                throw new RespExp(401,"Error: Unauthorized logout attempt");
            }
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error" + e.getMessage());
        }
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
