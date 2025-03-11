package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SQLUserDAO implements UserDAO {
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS user_data (
                username VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255),
                PRIMARY KEY (username)
            )
            """

    };
    public SQLUserDAO() throws DataAccessException {
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
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

        var statment = "INSERT INTO user_data (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(statment)){
                pstmt.setString(1, user.username());
                pstmt.setString(2, user.password());
                pstmt.setString(3, user.email());
                pstmt.executeUpdate();
            }

        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }

    }



    @Override
    public void clear() throws DataAccessException {

    }
}
