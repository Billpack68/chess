package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

import java.sql.SQLException;
import java.util.Objects;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void addUser(UserData userData) throws AlreadyTakenException, SQLException, DataAccessException {
        String username = userData.username();
        if (getUserByUsername(username) != null) {
            throw new AlreadyTakenException("Error: already taken");
        }

        userDAO.addUserData(userData);
    }

    public void loginUser(String username, String password) throws InvalidCredentialsException {
        UserData existingUser = getUserByUsername(username);
        if (existingUser == null || !Objects.equals(existingUser.password(), password)) {
            throw new InvalidCredentialsException("Error: unauthorized");
        }
    }

    public void deleteUserData() {
        userDAO.deleteUserData();
    }

    public UserData getUserByUsername(String username) {
        return userDAO.getUser(username);
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
