package dataaccess;
import model.UserData;

import java.util.HashSet;
import java.util.Set;

public class MemUserDAO implements UserDAO {

    private Set<UserData> userStorage;

    public MemUserDAO() {
        userStorage = new HashSet<>();
    }

    @Override
    public UserData getUser(String username) {
        return userStorage.stream()
                .filter(user -> user.username().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if(!userStorage.contains(user)) {
            userStorage.add(user);
        }
        else{
            throw new DataAccessException("User already exists");
        }
    }




    @Override
    public void clear() {
        userStorage.clear();
    }
}
