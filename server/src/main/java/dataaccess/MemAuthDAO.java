package dataaccess;

import model.AuthData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MemAuthDAO implements AuthDAO {

    private Set<AuthData> authStorage;

    public MemAuthDAO() {
        authStorage = new HashSet<>();
    }

    @Override
    public void addAuth(String authToken, String username) {
        authStorage.add(new AuthData(username, authToken));
    }

    @Override
    public AuthData addAuth(AuthData authData) {
        authData=new AuthData(authData.username(), UUID.randomUUID().toString());
        authStorage.add(authData);
        return authData;
    }

    @Override
    public void deleteAuth(String authToken) {
        authStorage.removeIf(auth -> auth.authToken().equals(authToken));
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authStorage.stream()
                .filter(auth -> auth.authToken().equals(authToken))
                .findFirst()
                .orElseThrow(() -> new DataAccessException("Auth Token does not exist: " + authToken));
    }

    @Override
    public void clear() {
        authStorage.clear();
    }
}
