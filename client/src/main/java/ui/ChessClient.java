package ui;

import model.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class ChessClient {
    private String clientName;
    private String authToken;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println();
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public String eval(String input) {
        String[] tokens = input.toLowerCase().split(" ");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "register" -> register(params);
            case "login" -> login(params);
//            case "logout" -> logout();
//            case "list" -> listGames();
//            case "create" -> createGame(params);
//            case "join" -> joinGame(params);
//            case "clear" -> clearDB();
            case "quit" -> "quit";
            default -> help();
        };

    }

    private void printPrompt() {
        System.out.print("\n>>> ");
    }

    private String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    Commands:
                    - register [username] [password] [email]
                    (Create a new account and log in to it)
                    - login [username] [password]
                    (Login to an existing account)
                    - help
                    (Shows a list of commands you can do)
                    - quit
                    (exits the program)
                    """;
        }
        return """
                - list
                - adopt <pet id>
                - rescue <name> <CAT|DOG|FROG|FISH>
                - adoptAll
                - signOut
                - quit
                """;
    }

    private String register(String... params) {
        try {
            assertNotSignedIn();
        } catch (Exception e) {
            return e.getMessage();
        }
        if (params.length != 3) {
            return "Expected: register [username] [password] [email]";
        } else {
            try {
                RegisterRequest request = new RegisterRequest(params[0], params[1], params[2]);
                server.register(request);
            } catch (ServerFacadeException e) {
                if (e.getId() == 400) {
                    return "Expected: register [username] [password] [email]";
                } else if (e.getId() == 403) {
                    return "Sorry, that username is already in use";
                } else {
                    return "Oops! Looks like something went wrong with registering. Can you try again?";
                }
            }

            return login(params[0], params[1]);
        }
    }

    private String login(String... params) {
        try {
            assertNotSignedIn();
        } catch (Exception e) {
            return e.getMessage();
        }
        if (params.length != 2) {
            return "Expected: login [username] [password]";
        }
        try {
            LoginRequest request = new LoginRequest (params[0], params[1]);
            authToken = server.login(request).authToken();
        } catch (ServerFacadeException e) {
            if (e.getId() == 400) {
                return "Expected: login [username] [password]";
            } else if (e.getId() == 401) {
                return "Username or password invalid";
            } else {
                return "Oops! Looks like something went wrong with logging in. Can you try again?";
            }
        }
        return "Logged in as " + params[0] + ". Type `help` to see a list of commands.";
    }

    private void assertSignedIn() throws Exception {
        if (state == State.SIGNEDOUT) {
            throw new Exception("You must sign in first");
        }
    }

    private void assertNotSignedIn() throws Exception {
        if (state != State.SIGNEDOUT) {
            throw new Exception("You must be signed out to use that command");
        }
    }
}
