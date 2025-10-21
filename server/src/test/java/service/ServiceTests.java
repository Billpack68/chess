package service;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private MemoryAuthDAO authDAO;
    private MemoryUserDAO userDAO;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        authDAO = new MemoryAuthDAO();
        authService = new AuthService(authDAO);
        userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO);
    }

    @Test
    void testClearDB() {
        AuthData dummyAuth = new AuthData("token1", "user1");
        UserData dummyUser = new UserData("username", "password", "email");

        authService.createAuth(dummyAuth);
        userService.createUser(dummyUser);

        assertNotNull(authService.getAuth(dummyAuth));
        assertNotNull(userService.getUser(dummyUser));

        authService.deleteAuthData();
        userService.deleteUserData();

        assertNull(authService.getAuth(dummyAuth));
        assertNull(userService.getUser(dummyUser));
    }
}
