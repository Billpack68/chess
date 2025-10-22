package service;
import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
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

    @Test
    void testListGamesPositive() throws InvalidAuthTokenException, MissingDataException {
        RegisterRequest dummyRegister = new RegisterRequest("username", "password", "email");
        service.register(dummyRegister);
        LoginRequest dummyLogin = new LoginRequest("username", "password");
        String authToken = service.login(dummyLogin).authToken();
        CreateGameRequest dummyCreate = new CreateGameRequest(authToken, "GAMENAME");
        service.createGame(dummyCreate);
        service.createGame(dummyCreate);
        service.createGame(dummyCreate);
        ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
        ListGamesResult listGamesResult = service.listGames(listGamesRequest);
        Collection<GameData> gameData = listGamesResult.gameData();
        assert(gameData.size() == 3);
        GameData game1 = new GameData(1, null, null,
                "GAMENAME", new ChessGame());
        GameData game2 = new GameData(2, null, null,
                "GAMENAME", new ChessGame());
        GameData game4 = new GameData(4, null, null,
                "GAMENAME", new ChessGame());
        GameData game0 = new GameData(0, null, null,
                "GAMENAME", new ChessGame());
        assert(gameData.contains(game1));
        assert(gameData.contains(game2));
        assert(!gameData.contains(game4));
        assert(!gameData.contains(game0));
    }

    @Test
    void testListGamesInvalidAuthToken() throws InvalidAuthTokenException, MissingDataException {
        RegisterRequest dummyRegister = new RegisterRequest("username", "password", "email");
        service.register(dummyRegister);
        LoginRequest dummyLogin = new LoginRequest("username", "password");
        service.login(dummyLogin);
        String authToken = "fakeToken";
        ListGamesRequest dummyList = new ListGamesRequest(authToken);
        assertThrows(InvalidAuthTokenException.class, () -> {
            service.listGames(dummyList);
        });
    }

    @Test
    void testClearDB() throws MissingDataException, InvalidAuthTokenException {
        // Copying the code from the test for list games in order to populate my DB
        RegisterRequest dummyRegister = new RegisterRequest("username", "password", "email");
        service.register(dummyRegister);
        LoginRequest dummyLogin = new LoginRequest("username", "password");
        String authToken = service.login(dummyLogin).authToken();
        CreateGameRequest dummyCreate = new CreateGameRequest(authToken, "GAMENAME");
        service.createGame(dummyCreate);
        service.createGame(dummyCreate);
        service.createGame(dummyCreate);

        // Now the real test
        ClearDatabaseResult clearResult = service.clearDB(new ClearDatabaseRequest());
        assertNotNull(clearResult);
    }
}
