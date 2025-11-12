package client;

import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;
import ui.ServerFacadeException;

import java.util.Objects;

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
        RegisterResult result = serverFacade.register(request);
        assertThrows(ServerFacadeException.class, () -> {
            serverFacade.register(request);
        });
    }

}
