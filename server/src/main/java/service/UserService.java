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

    public void addUser(UserData userData) throws AlreadyTakenException {
        String username = userData.username();
        if (getUserByUsername(username) != null) {
            throw new AlreadyTakenException("Error: already taken");
        }

        userDAO.addUserData(userData);
    }

    public void loginUser(String username, String password) throws InvalidCredentialsException {
        UserData existingUser = getUserByUsername(username);
        if (existingUser == null || !Objects.equals(existingUser.password(), password)) {
            throw new InvalidCredentialsException("Invalid username/password");
        }
    }

    public void deleteUserData() {
        userDAO.deleteUserData();
    }

    public UserData getUserByUsername(String username) {
        return userDAO.getUser(username);
//        if (existingUser != null) {
//            throw new AlreadyTakenException("Username already exists");
//        }
//        UserData newUser = new UserData(registerRequest.username(), registerRequest.password(),
//                registerRequest.email());
//        createUser(newUser);
//        return true;
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
