package service;
import java.util.*;
import dataaccess.*;
import model.*;
import java.util.*;
public class UserService {
    UserDAO userDAO;
    AuthDAO authDAO;


    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}
