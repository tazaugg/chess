package service;
import java.util.*;
import dataaccess.*;
import model.*;
import java.util.*;

public class GameService {
    GameDAO gameDAO;
    AuthDAO authDAO;


    public void clear() {
        gameDAO.clear();
    }
}
