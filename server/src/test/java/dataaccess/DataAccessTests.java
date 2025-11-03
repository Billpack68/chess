package dataaccess;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import service.MissingDataException;

import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataAccessTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    @BeforeEach
    void setUp() throws ResponseException, DataAccessException {
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        userDAO.deleteUserData();
    }

    @Test
    void TestRegisterPositive() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        UserData user = userDAO.getUser("username");
        assert(Objects.equals(newUser.username(), user.username()));
        assert(BCrypt.checkpw(newUser.password(), user.password()));
        assert(Objects.equals(newUser.email(), user.email()));
    }

    @Test
    void TestRegisterDuplicateUsername() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        UserData sameUsername = new UserData("username", "password2", "email2");
        assertThrows(DataAccessException.class, () -> {
            userDAO.addUserData(sameUsername);
        });
    }
}
