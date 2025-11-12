package client;

import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;
import ui.ServerFacadeException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    void cleanDatabase() {
        serverFacade.clearDB(new ClearDatabaseRequest());
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void testRegisterPositive() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        RegisterResult result = serverFacade.register(request);
        assert(Objects.equals(result.username(), "username"));
    }

    @Test
    public void testRegisterUsernameTaken() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        assertThrows(ServerFacadeException.class, () -> {
            serverFacade.register(request);
        });
    }

    @Test
    public void testRegisterUsernameNoEmail() {
        RegisterRequest request = new RegisterRequest("username", "password", null);
        assertThrows(ServerFacadeException.class, () -> {
            serverFacade.register(request);
        });
    }

    @Test
    public void testLoginUserPositive() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult result = serverFacade.login(request2);
        assert(Objects.equals(result.username(), "username") && result.authToken() != null);
    }

    @Test
    public void testLoginMissingPassword() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", null);
        assertThrows(ServerFacadeException.class, () -> {
            serverFacade.login(request2);
        });
    }

    @Test
    public void testLoginIncorrectPassword() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "passwork");
        assertThrows(ServerFacadeException.class, () -> {
            serverFacade.login(request2);
        });
    }

    @Test
    public void testLogoutPositive() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        LogoutRequest request3 = new LogoutRequest(loginResult.authToken());
        assertDoesNotThrow(() -> serverFacade.logout(request3));
    }

    @Test
    public void testLogoutWrongAuth() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        serverFacade.login(request2);
        LogoutRequest request3 = new LogoutRequest("fakeAuthToken");
        assertThrows(ServerFacadeException.class, () -> {
            serverFacade.logout(request3);
        });
    }

    @Test
    public void testCreateGamePositive() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), "testGame");
        CreateGameResult createGameResult = serverFacade.createGame(request3);
        System.out.println(createGameResult.gameID());
        assert(createGameResult.gameID() != 0);
    }

    @Test
    public void testCreateGameMissingName() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), null);
        assertThrows(ServerFacadeException.class, () -> {
            serverFacade.createGame(request3);
        });
    }

    @Test
    public void testCreateGameInvalidAuth() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest("badAuthToken", "gameName");
        assertThrows(ServerFacadeException.class, () -> {
            serverFacade.createGame(request3);
        });
    }

    @Test
    public void testListGamesPositive() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), "testGame");
        serverFacade.createGame(request3);
        ListGamesRequest request4 = new ListGamesRequest(loginResult.authToken());
        ListGamesResult listGamesResult = serverFacade.listGames(request4);
        assert(listGamesResult.games().size() == 1);
        serverFacade.createGame(request3);
        listGamesResult = serverFacade.listGames(request4);
        assert(listGamesResult.games().size() == 2);
    }

    @Test
    public void testListGamesInvalidAuth() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), "testGame");
        serverFacade.createGame(request3);
        ListGamesRequest request4 = new ListGamesRequest("badAuth");
        assertThrows(ServerFacadeException.class, () -> {
            serverFacade.listGames(request4);
        });
    }

    @Test
    public void testJoinGamePositive() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), "testGame");
        CreateGameResult createGameResult = serverFacade.createGame(request3);
        ListGamesRequest request4 = new ListGamesRequest(loginResult.authToken());
        serverFacade.listGames(request4);
        JoinGameRequest request5 = new JoinGameRequest(loginResult.authToken(), "WHITE",
                createGameResult.gameID());
        assertDoesNotThrow(() -> serverFacade.joinGame(request5));
    }

    @Test
    public void testJoinGameWrongGameNumber() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), "testGame");
        CreateGameResult createGameResult = serverFacade.createGame(request3);
        JoinGameRequest request4 = new JoinGameRequest(loginResult.authToken(), "WHITE",
                createGameResult.gameID() + 1);
        assertThrows(ServerFacadeException.class, () -> serverFacade.joinGame(request4));
    }

    @Test
    public void testJoinGameWrongAuthToken() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), "testGame");
        CreateGameResult createGameResult = serverFacade.createGame(request3);
        JoinGameRequest request4 = new JoinGameRequest("authToken?", "WHITE",
                createGameResult.gameID());
        assertThrows(ServerFacadeException.class, () -> serverFacade.joinGame(request4));
    }

    @Test
    public void testJoinGameMissingData() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), "testGame");
        serverFacade.createGame(request3);
        JoinGameRequest request4 = new JoinGameRequest(loginResult.authToken(), "WHITE",
                null);
        assertThrows(ServerFacadeException.class, () -> serverFacade.joinGame(request4));
    }

    @Test
    public void testJoinGameColorTaken() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), "testGame");
        CreateGameResult createGameResult = serverFacade.createGame(request3);
        JoinGameRequest request4 = new JoinGameRequest(loginResult.authToken(), "WHITE",
                createGameResult.gameID());
        serverFacade.joinGame(request4);
        assertThrows(ServerFacadeException.class, () -> serverFacade.joinGame(request4));
    }

    @Test
    public void testClearDB() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        serverFacade.register(request);
        LoginRequest request2 = new LoginRequest("username", "password");
        LoginResult loginResult = serverFacade.login(request2);
        CreateGameRequest request3 = new CreateGameRequest(loginResult.authToken(), "testGame");
        serverFacade.createGame(request3);
        serverFacade.clearDB(new ClearDatabaseRequest());
        assertThrows(ServerFacadeException.class, () -> serverFacade.login(request2));
        ListGamesRequest request4 = new ListGamesRequest(loginResult.authToken());
        assertThrows(ServerFacadeException.class, () -> serverFacade.listGames(request4));
    }


}
