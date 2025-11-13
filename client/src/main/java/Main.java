import chess.*;
import server.Server;
import ui.BoardPrinter;
import ui.ChessClient;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }
        try {
            Server server = new Server();
            server.run(8080);

            ChessClient client = new ChessClient(serverUrl);
            client.run();

            server.stop();

        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }
}