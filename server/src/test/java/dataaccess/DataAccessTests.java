package dataaccess;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import service.AuthService;
import service.MissingDataException;

import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNull;
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
        authDAO.deleteAuthData();
    }

    @Test
    void TestAddUserPositive() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        UserData user = userDAO.getUser("username");
        assert(Objects.equals(newUser.username(), user.username()));
        assert(BCrypt.checkpw(newUser.password(), user.password()));
        assert(Objects.equals(newUser.email(), user.email()));
    }

    @Test
    void TestAddUserUsernameTaken() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        UserData sameUsername = new UserData("username", "password2", "email2");
        assertThrows(DataAccessException.class, () -> {
            userDAO.addUserData(sameUsername);
        });
    }

    @Test
    void TestGetUserPositive() throws SQLException, DataAccessException {
        UserData newUser = new UserData("fancyUsername", "password", "email");
        userDAO.addUserData(newUser);
        UserData user = userDAO.getUser("fancyUsername");
        assert(Objects.equals(newUser.username(), user.username()));
    }

    @Test
    void TestGetUserInvalidUsername() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        assertNull(userDAO.getUser("differentUsername"));
    }

    @Test
    void TestDeleteUserData() throws SQLException, DataAccessException {
        UserData newUser = new UserData("fancyUsername", "password", "email");
        userDAO.addUserData(newUser);
        userDAO.deleteUserData();
        assertNull(userDAO.getUser("fancyUsername"));
    }

    @Test
    void TestAddAuthData() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);

        AuthService authService = new AuthService(authDAO);

        AuthData result = authService.createAuth("username");
        // Either it throws an error or it returns AuthData with a token and a username, I don't know what
        // the authToken is, but I can compare usernames!
        assert(Objects.equals(result.username(), "username"));
    }
}
