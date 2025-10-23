package handler;

import com.google.gson.Gson;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.*;
import service.*;

import io.javalin.http.Context;

public class Handler {

    private final Service service;
    private Gson serializer;

    public Handler() {
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        AuthService authService = new AuthService(authDAO);
        MemoryUserDAO userDAO = new MemoryUserDAO();
        UserService userService = new UserService(userDAO);
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        GameService gameService = new GameService(gameDAO);
        this.service = new Service(authService, gameService, userService);
        this.serializer = new Gson();
    }

    public void registerUser(Context ctx) throws MissingDataException {
        RegisterRequest request = serializer.fromJson(ctx.body(), RegisterRequest.class);
        RegisterResult result = service.register(request);
        ctx.result(serializer.toJson(result));
        ctx.status(200);
    }

    public void loginUser(Context ctx) throws MissingDataException {
        LoginRequest request = serializer.fromJson(ctx.body(), LoginRequest.class);
        LoginResult result = service.login(request);
        ctx.result(serializer.toJson(result));
        ctx.status(200);
    }

    public void logoutUser(Context ctx) throws InvalidAuthTokenException {
        LogoutRequest request = serializer.fromJson(ctx.body(), LogoutRequest.class);
        LogoutResult result = service.logout(request);
        ctx.status(200);
    }

    public void listGames(Context ctx) throws InvalidAuthTokenException {
        ListGamesRequest request = serializer.fromJson(ctx.body(), ListGamesRequest.class);
        ListGamesResult result = service.listGames(request);
        ctx.result(serializer.toJson(result));
        ctx.status(200);
    }

    public void createGame(Context ctx) throws MissingDataException, InvalidAuthTokenException {
        CreateGameRequest request = serializer.fromJson(ctx.body(), CreateGameRequest.class);
        CreateGameResult result = service.createGame(request);
        ctx.result(serializer.toJson(result));
        ctx.status(200);
    }

    public void joinGame(Context ctx) throws MissingDataException, InvalidAuthTokenException,
            GameNotFoundException, ColorAlreadyTakenException {
        JoinGameRequest request = serializer.fromJson(ctx.body(), JoinGameRequest.class);
        JoinGameResult result = service.joinGame(request);
        ctx.result(serializer.toJson(result));
        ctx.status(200);
    }

    public void clearDB(Context ctx) {
        ClearDatabaseRequest request = serializer.fromJson(ctx.body(), ClearDatabaseRequest.class);
        ClearDatabaseResult result = service.clearDB(request);
        ctx.result(serializer.toJson(result));
        ctx.status(200);
    }
}
