package service;

import dataaccess.DataAccessException;
import model.*;

import java.sql.SQLException;
import java.util.Objects;

public class Service {
    private final AuthService authService;
    private final GameService gameService;
    private final UserService userService;

    public Service(AuthService authService, GameService gameService, UserService userService) {
        this.authService = authService;
        this.gameService = gameService;
        this.userService = userService;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws AlreadyTakenException,
            MissingDataException, SQLException, DataAccessException {
        String username = registerRequest.username();
        String password = registerRequest.password();
        String email = registerRequest.email();
        if (username == null || password == null || email == null) {
            throw new MissingDataException("Error: bad request");
        }
        UserData userData = new UserData(username, password, email);
        userService.addUser(userData);
        AuthData userAuth = authService.createAuth(username);
        return new RegisterResult(userAuth.username(), userAuth.authToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws MissingDataException, InvalidCredentialsException, SQLException, DataAccessException {
        String username = loginRequest.username();
        String password = loginRequest.password();
        if (username == null || password == null) {
            throw new MissingDataException("Error: bad request");
        }
        userService.loginUser(username, password);
        AuthData userAuth = authService.createAuth(username);
        return new LoginResult(userAuth.username(), userAuth.authToken());
    }

    public LogoutResult logout(LogoutRequest logoutRequest) throws InvalidAuthTokenException {
        String authToken = logoutRequest.authToken();
        AuthData authData = authService.getAuth(authToken);
        authService.deleteAuthToken(authData.authToken());
        return new LogoutResult();
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) throws InvalidAuthTokenException {
        String authToken = listGamesRequest.authToken();
        AuthData authData = authService.getAuth(authToken);
        return new ListGamesResult(gameService.getGames());
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws
            MissingDataException, InvalidAuthTokenException {
        String authToken = createGameRequest.authToken();
        String gameName = createGameRequest.gameName();
        if (authToken == null || gameName == null) {
            throw new MissingDataException("Error: bad request");
        }

        AuthData authData = authService.getAuth(authToken);
        int gameID = gameService.createGame(gameName);
        return new CreateGameResult(gameID);
    }

    public JoinGameResult joinGame(JoinGameRequest joinGameRequest) throws MissingDataException,
            InvalidAuthTokenException, GameNotFoundException, ColorAlreadyTakenException {
        String authToken = joinGameRequest.authToken();
        String playerColor = joinGameRequest.playerColor();
        Integer gameID = joinGameRequest.gameID();
        if (authToken == null || gameID == null ||
                (!Objects.equals(playerColor, "BLACK") && !Objects.equals(playerColor, "WHITE"))) {
            throw new MissingDataException("Error: bad request");
        }

        AuthData authData = authService.getAuth(authToken);
        GameData gameData = gameService.findGameDataByID(gameID);
        if ((playerColor.equals("BLACK") && gameData.blackUsername() != null)
                || (playerColor.equals("WHITE") && gameData.whiteUsername() != null)) {
            throw new ColorAlreadyTakenException("Error: already taken");
        }

        gameService.joinGame(gameData, playerColor, authData.username());
        return new JoinGameResult();
    }

    public ClearDatabaseResult clearDB(ClearDatabaseRequest request) throws DataAccessException {
        userService.deleteUserData();
        authService.deleteAuthData();
        gameService.deleteGameData();
        return new ClearDatabaseResult();
    }
}
