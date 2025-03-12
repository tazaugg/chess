package dataaccess;

import model.AuthData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO {

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth_data (
                username VARCHAR(255) NOT NULL,
                authToken VARCHAR(255) NOT NULL UNIQUE,
                PRIMARY KEY (authToken)
            )
            """

    };
    public SQLAuthDAO() throws DataAccessException {
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
    public AuthData addAuth(AuthData authData) throws DataAccessException {
        authData = new AuthData(authData.username(), UUID.randomUUID().toString());
        var statement = "INSERT INTO auth_data (username, authToken) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(statement)){
                pstmt.setString(1, authData.username());
                pstmt.setString(2, authData.authToken());
                pstmt.executeUpdate();
            }

        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }
        return authData;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth_data WHERE authToken = ?";
        try(var conn= DatabaseManager.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(statement)){
                pstmt.setString(1, authToken);
                pstmt.executeUpdate();
            }

        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statment = "SELECT username, authToken FROM auth_data WHERE authToken = ?";
        try(var conn = DatabaseManager.getConnection()){
            try(PreparedStatement pstmt = conn.prepareStatement(statment)){
                pstmt.setString(1, authToken);
                try(ResultSet rs = pstmt.executeQuery()){
                    if(rs.next()){
                        return new AuthData(rs.getString("username"), rs.getString("authToken"));
                    }
                }
            }
        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        var statment = "TRUNCATE  auth_data";
        try(var conn= DatabaseManager.getConnection()) {
            try(PreparedStatement pstmt = conn.prepareStatement(statment)){
                pstmt.executeUpdate();
            }

        }
        catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }

    }
}
