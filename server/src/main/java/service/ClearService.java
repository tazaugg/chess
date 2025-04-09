package service;

import dataaccess.DataAccessException;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import exceptions.RespExp;

public class ClearService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;

    public ClearService(AuthDAO authDAO, UserDAO userDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
    }

    public void clear() throws RespExp {
        try{
            authDAO.clear();
            userDAO.clear();
            gameDAO.clear();
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error:" + e.getMessage());
        }
    }


}