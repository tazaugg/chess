package dataaccess;

import java.util.HashMap;
import java.util.Map;


public class DataAccess {
    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, String> authTokens = new HashMap<>();
    private static final Map<Integer, String> games = new HashMap<>();


    public void clearAll() {
        users.clear();
        authTokens.clear();
        games.clear();
        System.out.println("Database cleared.");
    }
}
