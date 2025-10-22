package service;
import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private Service service;

    @BeforeEach
    void setUp() {
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        AuthService authService = new AuthService(authDAO);
        MemoryUserDAO userDAO = new MemoryUserDAO();
        UserService userService = new UserService(userDAO);
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        GameService gameService = new GameService(gameDAO);
        service = new Service(authService, gameService, userService);
    }

    @Test
    void testRegisterPositive() throws MissingDataException, AlreadyTakenException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        RegisterResult result = service.register(registerRequest);
        assert(Objects.equals(result.username(), "username"));
    }

    //    private AuthService authService;
//    private UserService userService;
//    private GameService gameService;
//
//    @BeforeEach
//    void setUp() {
//        MemoryAuthDAO authDAO = new MemoryAuthDAO();
//        authService = new AuthService(authDAO);
//        MemoryUserDAO userDAO = new MemoryUserDAO();
//        userService = new UserService(userDAO);
//        MemoryGameDAO gameDAO = new MemoryGameDAO();
//        gameService = new GameService(gameDAO);
//    }
//
//    @Test
//    void testClearDB() {
//        AuthData dummyAuth = new AuthData("token1", "user1");
//        UserData dummyUser = new UserData("username", "password", "email");
//        GameData dummyGame = new GameData(1, "white", "black",
//                "name", new ChessGame());
//
//        authService.addAuth(dummyAuth);
//        userService.addUser(dummyUser);
//        gameService.createGame(dummyGame);
//
//        assertNotNull(authService.getAuth(dummyAuth));
//        assertNotNull(userService.getUser(dummyUser));
//        assertNotNull(gameService.getGame(dummyGame));
//
//        authService.deleteAuthData();
//        userService.deleteUserData();
//        gameService.deleteGameData();
//
//        assertNull(authService.getAuth(dummyAuth));
//        assertNull(userService.getUser(dummyUser));
//        assertNull(gameService.getGame(dummyGame));
//    }
}
