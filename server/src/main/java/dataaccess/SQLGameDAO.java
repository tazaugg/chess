package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import model.UserData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SQLGameDAO implements GameDAO {
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS game_data (
                gameID int NOT NULL AUTO_INCREMENT,
                gameName varchar(255) NOT NULL UNIQUE,
                json text NOT NULL,
                PRIMARY KEY (gameID),
                INDEX(gameName)
            )
            """
    };
    public SQLGameDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        try(var conn = DatabaseManager.getConnection()){
            for(var stmt : createStatements){
                try(var pstmt = conn.prepareStatement(stmt)){
                    pstmt.executeUpdate();
                }
            }
        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }
    @Override
    public Collection<GameData> listGames() throws DataAccessException{
        var games = new ArrayList<GameData>();
        var stmt = "SELECT gameID, json FROM game_data";
        try(var conn=DatabaseManager.getConnection()){
            try(var pstmt = conn.prepareStatement(stmt)){
                try(var rs = pstmt.executeQuery()){
                    while(rs.next()){
                        games.add(readGame(rs));
                    }
                }
            }
        }
        catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
        return games;
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO game_data (gameName, json) VALUES (?, ?)";
        var json= new Gson().toJson(game);
        try(var conn = DatabaseManager.getConnection()) {
            try(var pstmt = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)){
                pstmt.setString(1, game.gameName());
                pstmt.setString(2, json);
                pstmt.executeUpdate();
                var rs = pstmt.getGeneratedKeys();
                if(rs.next()){
                    return new GameData(
                            rs.getInt(1),
                            game.whiteUsername(),
                            game.blackUsername(),
                            game.gameName(),
                            game.game()
                    );
                }
            }

        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }
        return game;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT gameID, json FROM game_data WHERE gameID = ?";
        try(var conn = DatabaseManager.getConnection()){
            try(var pstmt = conn.prepareStatement(statement)){
                pstmt.setInt(1, gameID);
                try(var rs = pstmt.executeQuery()){
                    if(rs.next()){
                        return readGame(rs);
                    }
                }
            }
        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }

        return null;
    }
    private GameData readGame(ResultSet rs) throws SQLException {
        var id = rs.getInt("gameID");
        var json = rs.getString("json");
        var game = new Gson().fromJson(json, GameData.class);
        return game.setGameID(id);
    }

    @Override
    public boolean gameExists(int gameID) throws DataAccessException {
        var statement = "SELECT COUNT(*) FROM game_data WHERE gameID = ?";
        try(var conn = DatabaseManager.getConnection()){
            try(var pstmt = conn.prepareStatement(statement)){
                pstmt.setInt(1, gameID);
                try(var rs = pstmt.executeQuery()){
                    if(rs.next()){
                        return rs.getInt(1) == 1;
                    }
                }
            }
        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }
        return false;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var statement = "UPDATE game_data SET json = ? WHERE gameID = ?";
        var json= new Gson().toJson(game);
        try(var conn=DatabaseManager.getConnection()) {
            try(var pstmt = conn.prepareStatement(statement)){
                pstmt.setString(1, json);
                pstmt.setInt(2, game.gameID());
                pstmt.executeUpdate();
            }
        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }

    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE game_data";
        try(var conn = DatabaseManager.getConnection()){
            try(var pstmt = conn.prepareStatement(statement)){
                pstmt.executeUpdate();
            }
        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }

    }
}
