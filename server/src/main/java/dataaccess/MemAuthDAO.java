package dataaccess;

import model.AuthData;

import java.util.*;

public class MemAuthDAO implements AuthDAO {

    private Map<String, AuthData> authStorage;

    public MemAuthDAO() {
        authStorage = new HashMap<>();
    }



    @Override
    public AuthData addAuth(AuthData authData) throws DataAccessException {
        if(authData == null || authData.username() == null){
            throw new DataAccessException("No Username provided");
        }
        authData=new AuthData(authData.username(), UUID.randomUUID().toString());
        authStorage.put(authData.authToken(), authData);
        return authData;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

        if(authStorage.remove(authToken) == null){
            throw new DataAccessException("Can't remove what doesn't exist");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authStorage.get(authToken);
    }

    @Override
    public void clear() {
        authStorage.clear();
    }
}
