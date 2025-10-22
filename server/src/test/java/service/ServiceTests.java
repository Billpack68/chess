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

    @Test
    void testRegisterUsernameTaken() throws MissingDataException, AlreadyTakenException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        service.register(registerRequest);
        assertThrows(AlreadyTakenException.class, () -> {
            service.register(registerRequest);
        });
    }

    @Test
    void testRegisterMissingEmail() throws MissingDataException, AlreadyTakenException {
        RegisterRequest badRequest = new RegisterRequest("username", "password", null);
        assertThrows(MissingDataException.class, () -> {
            service.register(badRequest);
        });
    }

    @Test
    void testLoginPositive() throws MissingDataException, InvalidCredentialsException {
        RegisterRequest dummyRegister = new RegisterRequest("username", "password", "email");
        service.register(dummyRegister);
        LoginRequest dummyLogin = new LoginRequest("username", "password");
        LoginResult result = service.login(dummyLogin);
        assert(Objects.equals(result.username(), "username"));
    }

    @Test
    void testLoginInvalidUsername() throws MissingDataException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        service.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest("wrong", "password");
        assertThrows(InvalidCredentialsException.class, () -> {
            service.login(loginRequest);
        });
    }

    @Test
    void testLoginInvalidPassword() throws MissingDataException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        service.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest("username", "wrong");
        assertThrows(InvalidCredentialsException.class, () -> {
            service.login(loginRequest);
        });
    }

    @Test
    void testLoginMissingPassword() throws AlreadyTakenException {
        LoginRequest badRequest = new LoginRequest("username", null);
        assertThrows(MissingDataException.class, () -> {
            service.login(badRequest);
        });
    }

    @Test
    void testLogoutPositive() throws InvalidAuthTokenException, MissingDataException {
        RegisterRequest dummyRegister = new RegisterRequest("username", "password", "email");
        service.register(dummyRegister);
        LoginRequest dummyLogin = new LoginRequest("username", "password");
        LoginResult result = service.login(dummyLogin);
        String authToken = result.authToken();
        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        LogoutResult logoutResult = service.logout(logoutRequest);
        assertNotNull(logoutRequest);
    }

    @Test
    void testLogoutInvalidAuthToken() throws MissingDataException {
        RegisterRequest dummyRegister = new RegisterRequest("username", "password", "email");
        service.register(dummyRegister);
        LoginRequest dummyLogin = new LoginRequest("username", "password");
        service.login(dummyLogin);
        LogoutRequest logoutRequest = new LogoutRequest("fakeToken");
        assertThrows(InvalidAuthTokenException.class, () -> {
            service.logout(logoutRequest);
        });
    }

    @Test
    void testCreateGamePositive() throws InvalidAuthTokenException, MissingDataException {
        RegisterRequest dummyRegister = new RegisterRequest("username", "password", "email");
        service.register(dummyRegister);
        LoginRequest dummyLogin = new LoginRequest("username", "password");
        LoginResult result = service.login(dummyLogin);
        String authToken = result.authToken();
        CreateGameRequest dummyCreate = new CreateGameRequest(authToken, "GAMENAME");
        CreateGameResult createResult1 = service.createGame(dummyCreate);
        CreateGameResult createResult2 = service.createGame(dummyCreate);
        CreateGameResult createResult3 = service.createGame(dummyCreate);
        assert(createResult1.gameID() == 1);
        assert(createResult2.gameID() == 2);
        assert(createResult3.gameID() == 3);
    }

    @Test
    void testCreateGameInvalidAuthToken() throws InvalidAuthTokenException, MissingDataException {
        RegisterRequest dummyRegister = new RegisterRequest("username", "password", "email");
        service.register(dummyRegister);
        LoginRequest dummyLogin = new LoginRequest("username", "password");
        service.login(dummyLogin);
        String authToken = "fakeToken";
        CreateGameRequest dummyCreate = new CreateGameRequest(authToken, "GAMENAME");
        assertThrows(InvalidAuthTokenException.class, () -> {
            service.createGame(dummyCreate);
        });
    }

    @Test
    void testCreateGameMissingData() throws InvalidAuthTokenException, MissingDataException {
        RegisterRequest dummyRegister = new RegisterRequest("username", "password", "email");
        service.register(dummyRegister);
        LoginRequest dummyLogin = new LoginRequest("username", "password");
        service.login(dummyLogin);
        String authToken = "fakeToken";
        CreateGameRequest dummyCreate = new CreateGameRequest(authToken, null);
        assertThrows(MissingDataException.class, () -> {
            service.createGame(dummyCreate);
        });
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
