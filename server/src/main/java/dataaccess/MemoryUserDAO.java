package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.mindrot.jbcrypt.BCrypt.gensalt;

public class MemoryUserDAO extends UserDAO {
    private final Set<UserData> userData;

    public MemoryUserDAO() throws DataAccessException, ResponseException {
        this.userData = new HashSet<>();
    }

    public UserData addUserData(UserData newUserData) {
        String hashedPassword = BCrypt.hashpw(newUserData.password(), BCrypt.gensalt());
        userData.add(new UserData(newUserData.username(), hashedPassword, newUserData.email()));
        return newUserData;
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
