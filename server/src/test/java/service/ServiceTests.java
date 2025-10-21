package service;
import dataaccess.MemoryAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private MemoryAuthDAO authDAO;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authDAO = new MemoryAuthDAO();
        authService = new AuthService(authDAO);
    }

    @Test
    void testClearAuthTokens() {
        // Arrange: Add a couple tokens
        authService.createAuth(new AuthData("token1", "user1"));

        assertFalse(authService.getAuth(new AuthData("token1", "user1")) == null);

        // Act: Call the service to clear tokens
        authService.deleteAuthTokens();

        // Assert: Check that DAO's tokens are cleared
        assertTrue(authService.getAuth(new AuthData("token1", "user1")) == null,
                "DAO should be empty after clearAuthTokens() is called");
    }
}
