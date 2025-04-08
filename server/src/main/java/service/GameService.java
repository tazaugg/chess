package service;

import chess.ChessGame;
import dataaccess.*;
import exceptions.RespExp;
import model.*;

import java.util.Collection;
import java.util.Iterator;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public Collection<GameData> listGames(String authToken) throws RespExp {
        try{
            if (authDAO.getAuth(authToken) == null) {
                throw new RespExp(401, "Error: Unauthorized access");
            }
            return gameDAO.listGames();
        }
        catch (DataAccessException e) {
            throw new RespExp(500, "Error" + e.getMessage());
        }
    }

    public int createGame(String authToken, String gameName) throws RespExp {
       try{
           if (authDAO.getAuth(authToken) == null) {
               throw new RespExp(401, "Error: Invalid authentication");
           }
           Collection<GameData> games = gameDAO.listGames();
           Iterator<GameData> iter = games.iterator();
           while(iter.hasNext()) {
               GameData game = iter.next();
               if(game.gameName().equals(gameName)) {
                   throw new RespExp(400, "Error: bad request");
               }
           }

           GameData newGame = new GameData(0, null, null, gameName, new ChessGame());
           return gameDAO.createGame(newGame).gameID();
       }
       catch (DataAccessException e) {
           throw new RespExp(500, "Error" + e.getMessage());
       }
    }

    public int joinGame(String authToken, int gameID, String color) throws RespExp {
        try{
            AuthData userAuth = authDAO.getAuth(authToken);
            if (userAuth == null) {
                throw new RespExp(401, "Error: Invalid token provided");
            }

            if (!gameDAO.gameExists(gameID)) {
                throw new RespExp(400, "Error: bad request");// Game does not exist
            }

            GameData currentGame = gameDAO.getGame(gameID);
            String whitePlayer = currentGame.whiteUsername();
            String blackPlayer = currentGame.blackUsername();

            if ("WHITE".equalsIgnoreCase(color)) {
                if (whitePlayer != null) {throw new RespExp(403, "Error: already taken");}
                whitePlayer = userAuth.username();
            } else if ("BLACK".equalsIgnoreCase(color)) {
                if (blackPlayer != null) {throw new RespExp(403, "Error: already taken");}
                blackPlayer = userAuth.username();
            } else  {
                throw new RespExp(400, "Error: bad request"); // Invalid request
            }

            gameDAO.updateGame(new GameData(gameID, whitePlayer, blackPlayer, currentGame.gameName(), currentGame.game()));
            return 0;
        }
        catch (DataAccessException e) {
            throw new RespExp(500, "Error: " + e.getMessage());
        }
    }
    public GameData getGame(int gameID) throws RespExp {
        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error: " + e.getMessage());
        }
    }

    public void updateGame(GameData updatedGame) throws RespExp {
        try {
            gameDAO.updateGame(updatedGame);
        } catch (DataAccessException e) {
            throw new RespExp(500, "Error: " + e.getMessage());
        }
    }



    public void clear() throws RespExp {
        try{
            gameDAO.clear();
            //authDAO.clear();
        }
        catch(DataAccessException e){
            throw new RespExp(500, "Error" + e.getMessage());
        }
    }
}
