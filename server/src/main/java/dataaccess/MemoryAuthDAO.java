package dataaccess;

import model.AuthData;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MemoryAuthDAO extends AuthDAO {
    private final Set<AuthData> authData;

    public MemoryAuthDAO() throws DataAccessException {
        this.authData = new HashSet<>();
    }

    public AuthData addAuthData(AuthData newAuthData) {
        authData.add(newAuthData);
        return newAuthData;
    }

    public AuthData findAuthDataByAuthToken(String authToken) {
        for (AuthData data : authData) {
            if (Objects.equals(data.authToken(), authToken)) {
                return data;
            }
        }
        return null;
    }

    public void deleteAuthDataByAuthToken(String authToken) {
        String username = null;
        for (AuthData data : authData) {
            if (Objects.equals(data.authToken(), authToken)) {
                username = data.username();
            }
        }
        if (username != null) {
            authData.remove(new AuthData(authToken, username));
        }
    }

    public void deleteAuthData() {
        authData.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemoryAuthDAO that = (MemoryAuthDAO) o;
        return Objects.equals(authData, that.authData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(authData);
    }

    @Override
    public String toString() {
        return "MemoryAuthDAO{" +
                "authData=" + authData +
                '}';
    }
}
