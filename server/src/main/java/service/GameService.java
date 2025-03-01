package service;
import java.util.*;
import dataaccess.*;
import model.*;
import java.util.*;

public class GameService {
    GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public void clear() {
        gameDAO.clear();
    }
}
