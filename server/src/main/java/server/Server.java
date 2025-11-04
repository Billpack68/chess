package server;

import dataaccess.*;
import handler.Handler;
import io.javalin.*;
import org.eclipse.jetty.server.Response;

public class Server {

    private final Javalin javalin;
    private final Handler handler;

    public Server() {
        // Change this to false in order to use the real database
        boolean test = false;
        Handler tempHandler = null;
        if (test) {
            try {
                tempHandler = new Handler();
            } catch(DataAccessException ex) {
                System.err.println("Failed to initialize database or DAOs (for some reason): " + ex.getMessage());
                System.exit(1);
            }
        } else {
            try {
                tempHandler = new Handler(new UserDAO(), new AuthDAO(), new GameDAO());
            } catch(DataAccessException ex) {
                System.err.println("Failed to initialize database or DAOs: " + ex.getMessage());
                System.exit(1);
            }
        }

        handler = tempHandler;
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
