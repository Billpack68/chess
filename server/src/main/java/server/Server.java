package server;

import handler.Handler;
import io.javalin.*;

public class Server {

    private final Javalin javalin;
    private final Handler handler;

    public Server() {
        handler = new Handler();
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.post("/user", handler::registerUser);
        javalin.post("/session", handler::loginUser);
        javalin.delete("/session", handler::logoutUser);
        javalin.get("/game", handler::listGames);
        javalin.post("/game", handler::createGame);
        javalin.put("/game", handler::joinGame);
        javalin.delete("/db", handler::clearDB);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
