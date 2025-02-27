package service;

import dataaccess.DataAccess;

public class ClearService {
    public void clearAll() {
        DataAccess db = new DataAccess();
        db.clearAll();
    }
}
