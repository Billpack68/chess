package handler;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import service.*;

import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Handler {

    private final Service service;
    private Gson serializer;

    public Handler() throws DataAccessException {
        this(new MemoryUserDAO(), new MemoryAuthDAO(), new MemoryGameDAO());
    }

    public Handler(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        UserService userService = new UserService(userDAO);
        AuthService authService = new AuthService(authDAO);
        GameService gameService = new GameService(gameDAO);

        this.service = new Service(authService, gameService, userService);
        this.serializer = new Gson();
    }

    public void errorHandler(Context ctx, int status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        ctx.result(serializer.toJson(error));
        ctx.status(status);
    }

    public void registerUser(Context ctx) {
        try {
            RegisterRequest request = serializer.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResult result = service.register(request);
            ctx.result(serializer.toJson(result));
            ctx.status(200);
        } catch (MissingDataException e) {
            errorHandler(ctx, 400, e.getMessage());
        } catch (AlreadyTakenException e) {
            errorHandler(ctx, 403, e.getMessage());
        } catch (SQLException | DataAccessException e) {
            errorHandler(ctx, 500, e.getMessage());
        }
    }

    public void loginUser(Context ctx) {
        try {
            LoginRequest request = serializer.fromJson(ctx.body(), LoginRequest.class);
            LoginResult result = service.login(request);
            ctx.result(serializer.toJson(result));
            ctx.status(200);
        } catch (MissingDataException e) {
            errorHandler(ctx, 400, e.getMessage());
        } catch (InvalidCredentialsException e) {
            errorHandler(ctx, 401, e.getMessage());
        } catch (SQLException | DataAccessException e) {
            errorHandler(ctx, 500, e.getMessage());
        }

    }

    public void logoutUser(Context ctx) {
        try {
            LogoutRequest request = new LogoutRequest(ctx.header("authorization"));
            LogoutResult result = service.logout(request);
            ctx.status(200);
        } catch (InvalidAuthTokenException e) {
            errorHandler(ctx, 401, e.getMessage());
        } catch (SQLException | DataAccessException e) {
            errorHandler(ctx, 500, e.getMessage());
        }

    }

    public void listGames(Context ctx) {
        try {
            ListGamesRequest request = new ListGamesRequest(ctx.header("authorization"));
            ListGamesResult result = service.listGames(request);
            Collection<ListGamesData> formattedData = new HashSet<>();
            for (GameData data : result.gameData()) {
                formattedData.add(new ListGamesData(data.gameID(), data.gameName(),
                        data.whiteUsername(), data.blackUsername()));
            }
            Map<String, Object> response = Map.of("games", formattedData);

            ctx.result(serializer.toJson(response));
            ctx.status(200);
        } catch (InvalidAuthTokenException e) {
            errorHandler(ctx, 401, e.getMessage());
        } catch (SQLException | DataAccessException e) {
            errorHandler(ctx, 500, e.getMessage());
        }
    }

    public void createGame(Context ctx) {
        try {
            CreateGameRequest request = new CreateGameRequest(ctx.header("authorization"),
                    serializer.fromJson(ctx.body(), CreateGameName.class).gameName());
            CreateGameResult result = service.createGame(request);
            ctx.result(serializer.toJson(result));
            ctx.status(200);
        } catch (MissingDataException e) {
            errorHandler(ctx, 400, e.getMessage());
        } catch (InvalidAuthTokenException e) {
            errorHandler(ctx, 401, e.getMessage());
        } catch (SQLException | DataAccessException e) {
            errorHandler(ctx, 500, e.getMessage());
        }
    }

    public void joinGame(Context ctx) {
        try {
            CreateJoinGameRequest body = serializer.fromJson(ctx.body(), CreateJoinGameRequest.class);
            JoinGameRequest request = new JoinGameRequest(ctx.header("authorization"),
                    body.getPlayerColor(), body.getGameID());
            JoinGameResult result = service.joinGame(request);
            ctx.result(serializer.toJson(result));
            ctx.status(200);
        } catch (MissingDataException | GameNotFoundException e) {
            errorHandler(ctx, 400, e.getMessage());
        } catch (InvalidAuthTokenException e) {
            errorHandler(ctx, 401, e.getMessage());
        } catch (ColorAlreadyTakenException e) {
            errorHandler(ctx, 403, e.getMessage());
        }  catch (SQLException | DataAccessException e) {
            errorHandler(ctx, 500, e.getMessage());
        }
    }

    public void clearDB(Context ctx) {
        try {
            ClearDatabaseRequest request = serializer.fromJson(ctx.body(), ClearDatabaseRequest.class);
            ClearDatabaseResult result = service.clearDB(request);
            ctx.result(serializer.toJson(result));
            ctx.status(200);
        } catch (DataAccessException e) {
            errorHandler(ctx, 500, e.getMessage());
        }

    }
}
