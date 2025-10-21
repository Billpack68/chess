package service;

import dataaccess.MemoryUserDAO;
import model.UserData;

import java.util.Objects;

public class UserService {
    private final MemoryUserDAO userDAO;

    public UserService(MemoryUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserData getUser(UserData userData) {
        return userDAO.findUserData(userData);
    }

    public UserData createUser(UserData userData) {
        userDAO.addUserData(userData);
        return userData;
    }

    public void deleteUserData() {
        userDAO.deleteUserData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserService that = (UserService) o;
        return Objects.equals(userDAO, that.userDAO);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userDAO);
    }

    @Override
    public String toString() {
        return "UserService{" +
                "userDAO=" + userDAO +
                '}';
    }
}
