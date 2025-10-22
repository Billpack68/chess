package dataaccess;

import model.UserData;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MemoryUserDAO {
    private final Set<UserData> userData;

    public MemoryUserDAO() {
        this.userData = new HashSet<>();
    }

    public UserData addUserData(UserData newUserData) {
        userData.add(newUserData);
        return newUserData;
    }

    public UserData findUserData(UserData searchData) {
        if (userData.contains(searchData)) {
            return searchData;
        } else {
            return null;
        }
    }

    public void deleteUserData() {
        userData.clear();
    }

    public UserData getUser(String username) {
        for (UserData data : userData) {
            if (Objects.equals(data.username(), username)) {
                return data;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemoryUserDAO that = (MemoryUserDAO) o;
        return Objects.equals(userData, that.userData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userData);
    }

    @Override
    public String toString() {
        return "MemoryUserDAO{" +
                "userData=" + userData +
                '}';
    }
}
