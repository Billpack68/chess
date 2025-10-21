package dataaccess;

import model.AuthData;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MemoryAuthDAO {
    private Set<AuthData> authData = new HashSet<>();

    public MemoryAuthDAO() {
        this.authData = new HashSet<>();
    }

    public AuthData addAuthData(AuthData newAuthData) {
        authData.add(newAuthData);
        return newAuthData;
    }

    public AuthData findAuthData(AuthData searchData) {
        if (authData.contains(searchData)) {
            return searchData;
        } else {
            return null;
        }
    }

    public void deleteAuthTokens() {
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
