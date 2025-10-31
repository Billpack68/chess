package server;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import handler.Handler;
import io.javalin.*;

public class Server {

    private final Javalin javalin;
    private final Handler handler;

    public Server() {
        // Change this to false in order to use the real database
        boolean test = true;
        if (test) {
            handler = new Handler();
        } else {
            handler = new Handler(new AuthDAO(), new UserDAO(), new GameDAO());
        }
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
