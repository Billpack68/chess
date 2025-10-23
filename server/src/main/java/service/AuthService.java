package service;

import dataaccess.MemoryAuthDAO;
import model.AuthData;

import java.util.Objects;
import java.util.UUID;

public class AuthService {
    private final MemoryAuthDAO authDAO;

    public AuthService(MemoryAuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public AuthData getAuth(String authToken) throws InvalidAuthTokenException {
        AuthData returnData = authDAO.findAuthDataByAuthToken(authToken);
        if (returnData == null) {
            throw new InvalidAuthTokenException("Error: unauthorized");
        }
        return returnData;
    }

    public AuthData addAuth(AuthData authData) {
        authDAO.addAuthData(authData);
        return authData;
    }

    public AuthData createAuth(String username) {
        String authToken = generateToken();
        AuthData newAuthData = new AuthData(authToken, username);
        authDAO.addAuthData(newAuthData);
        return newAuthData;
    }

    public void deleteAuthData() {
        authDAO.deleteAuthData();
    }

    public void deleteAuthToken(String authToken) {
        authDAO.deleteAuthDataByAuthToken(authToken);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthService that = (AuthService) o;
        return Objects.equals(authDAO, that.authDAO);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(authDAO);
    }

    @Override
    public String toString() {
        return "AuthService{" +
                "authDAO=" + authDAO +
                '}';
    }
}
