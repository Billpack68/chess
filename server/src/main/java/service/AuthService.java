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

    public AuthData getAuth(AuthData authData) {
        return authDAO.findAuthData(authData);
    }

    public AuthData createAuth(AuthData authData) {
        authDAO.addAuthData(authData);
        return authData;
    }

    public void deleteAuthData() {
        authDAO.deleteAuthData();
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
