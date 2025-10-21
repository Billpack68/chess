package service;
import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private MemoryAuthDAO authDAO;
    private MemoryUserDAO userDAO;
    private MemoryGameDAO gameDAO;
    private AuthService authService;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        authDAO = new MemoryAuthDAO();
        authService = new AuthService(authDAO);
        userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO);
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO);
    }

    @Test
    void testClearDB() {
        AuthData dummyAuth = new AuthData("token1", "user1");
        UserData dummyUser = new UserData("username", "password", "email");
        GameData dummyGame = new GameData(1, "white", "black",
                "name", new ChessGame());

        authService.createAuth(dummyAuth);
        userService.createUser(dummyUser);
        gameService.createGame(dummyGame);

        assertNotNull(authService.getAuth(dummyAuth));
        assertNotNull(userService.getUser(dummyUser));
        assertNotNull(gameService.getGame(dummyGame));

        authService.deleteAuthData();
        userService.deleteUserData();
        gameService.deleteGameData();

        assertNull(authService.getAuth(dummyAuth));
        assertNull(userService.getUser(dummyUser));
        assertNull(gameService.getGame(dummyGame));
    }
}
