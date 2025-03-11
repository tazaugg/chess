package dataaccess;

import model.GameData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class SQLGameDAO implements GameDAO {
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS game_data (
                gameID int NOT NULL AUTO_INCREMENT,
                gameName varchar(255) NOT NULL UNIQUE,
                json text NOT NULL,
                PRIMARY KEY (gameID)
            )
            """
    };
    public SQLGameDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        try(var conn = DatabaseManager.getConnection()){
            for(var stmt : createStatements){
                try(PreparedStatement pstmt = conn.prepareStatement(stmt)){
                    pstmt.executeUpdate();
                }
            }
        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }
    @Override
    public Collection<GameData> listGames() {
        return List.of();
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {

        return null;
    }

    @Override
    public boolean gameExists(int gameID) throws DataAccessException {
        return false;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}
