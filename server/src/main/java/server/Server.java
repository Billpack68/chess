package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import dataaccess.*;
import handler.Handler;
import handler.WebSocketHandler;
import io.javalin.*;
import org.eclipse.jetty.server.Response;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class Server {

    private final Javalin javalin;
    private final Handler handler;
    private final Gson gson = createSerializer();
    private final WebSocketHandler wsHandler;

    public Server() {
        // Change this to false in order to use the real database
        boolean test = false;
        Handler tempHandler = null;
        WebSocketHandler tempWSHandler = null;
        if (test) {
            try {
                tempHandler = new Handler();
                tempWSHandler = new WebSocketHandler();
            } catch(DataAccessException ex) {
                System.err.println("Failed to initialize database or DAOs (for some reason): " + ex.getMessage());
                System.exit(1);
            }
        } else {
            try {
                tempHandler = new Handler(new UserDAO(), new AuthDAO(), new GameDAO());
                tempWSHandler = new WebSocketHandler();
            } catch(DataAccessException ex) {
                System.err.println("Failed to initialize database or DAOs: " + ex.getMessage());
                System.exit(1);
            }
        }

        handler = tempHandler;
        wsHandler = tempWSHandler;
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.post("/user", handler::registerUser);
        javalin.post("/session", handler::loginUser);
        javalin.delete("/session", handler::logoutUser);
        javalin.get("/game", handler::listGames);
        javalin.post("/game", handler::createGame);
        javalin.put("/game", handler::joinGame);
        javalin.delete("/db", handler::clearDB);

        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
                System.out.println("WebSocket connected");
            });

            ws.onMessage(ctx -> {
                try {
                    UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);

                    switch (command.getCommandType()) {
                        case CONNECT -> wsHandler.handleConnect(ctx, command.getAuthToken(), command.getGameID(),
                                ((ConnectCommand) command).getJoinType());
                    }



                } catch (Exception e) {
                    System.err.println("Error parsing WebSocket message: " + e.getMessage());
                }
            });

            ws.onClose(ctx -> System.out.println("WebSocket closed"));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        Gson defaultGson = new Gson();

        gsonBuilder.registerTypeAdapter(UserGameCommand.class,
            (JsonDeserializer<UserGameCommand>) (el, type, ctx) -> {
                UserGameCommand command = null;
                if (el.isJsonObject()) {
                    String commandType = el.getAsJsonObject().get("commandType").getAsString();
                    switch (UserGameCommand.CommandType.valueOf(commandType)) {
                        case RESIGN, LEAVE -> command = defaultGson.fromJson(el, UserGameCommand.class);
                        case MAKE_MOVE -> command = defaultGson.fromJson(el, MakeMoveCommand.class);
                        case CONNECT -> command = defaultGson.fromJson(el, ConnectCommand.class);
                    }
                }
                return command;
            });

        return gsonBuilder.create();
    }

    public void stop() {
        javalin.stop();
    }
}
