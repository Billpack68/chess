package ui;

import chess.ChessPiece;
import model.*;
import server.Server;

import java.util.*;

public class ChessClient {
    private String clientName;
    private String authToken;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private Map<Integer, Integer> IDConverter = new HashMap<>();

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
            case "logout" -> logout(params);
            case "list" -> listGames(params);
            case "create" -> createGame(params);
//            case "join" -> joinGame(params);
//            case "observe" -> observe(params);
            case "quit" -> "quit";
            default -> help();
        };

    }

    private void printPrompt() {
        if (clientName != null) {
            System.out.print("\n" + clientName + " >>> ");
        } else {
            System.out.print("\n>>> ");
        }
    }

    private String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    Commands:
                    register [username] [password] [email]
                        Create a new account and log in to it
                    login [username] [password]
                        Login to an existing account
                    help
                        Shows a list of commands you can do
                    quit
                        Exits the program
                    """;
        }

        return """
                Commands:
                logout
                    Logs you out
                list
                    Lists the games that you can join
                create [game-name]
                    Creates a new game under the name that you specify (no spaces)
                join [game-id] [color]
                    Join a game by specifying the game number
                help
                    Shows a list of commands you can do
                quit
                    Exits the program
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
        clientName = params[0];
        state = State.SIGNEDIN;
        return "Logged in as " + clientName + ". Type `help` to see a list of commands.";
    }

    private String logout(String... params) {
        try {
            assertSignedIn();
        } catch (Exception e) {
            return e.getMessage();
        }
        if (params.length != 0) {
            return "Too many arguments. Please just type `logout`";
        }
        try {
            LogoutRequest request = new LogoutRequest(authToken);
            server.logout(request);
        } catch (ServerFacadeException e) {
            if (e.getId() == 401) {
                return "Looks like you aren't authorized to do that. Please try again.";
            } else {
                return "Oops! Looks like something went wrong with logging out. Can you try again?";
            }
        }
        clientName = null;
        state = State.SIGNEDOUT;
        return "Successfully logged out!";
    }

    private String createGame(String... params) {
        try {
            assertSignedIn();
        } catch (Exception e) {
            return e.getMessage();
        }
        if (params.length != 1) {
            return "Expected: create [game-name]";
        }
        try {
            CreateGameRequest request = new CreateGameRequest(authToken, params[0]);
            server.createGame(request);
        } catch (ServerFacadeException e) {
            if (e.getId() == 400) {
                return "Expected: create [game-name]";
            } else if (e.getId() == 401) {
                return "Looks like you aren't authorized to do that. Please try again.";
            } else {
                return "Oops! Looks like something went wrong with logging out. Can you try again?";
            }
        }
        return "Successfully created game! Use `list` to see the details of that game.";
    }

    private String listGames(String... params) {
        try {
            assertSignedIn();
        } catch (Exception e) {
            return e.getMessage();
        }
        if (params.length != 0) {
            return "Too many arguments. Please just type `list`";
        }

        StringBuilder outString = new StringBuilder("Games currently being played:");
        try {
            ListGamesRequest request = new ListGamesRequest(authToken);
            ListGamesResult result = server.listGames(request);
            Collection<GameData> games = result.games();

            for (GameData data : games) {
                if (!IDConverter.containsKey(data.gameID())) {
                    IDConverter.put(data.gameID(), IDConverter.size()+1);
                }
                StringBuilder gameString = new StringBuilder("\n");
                gameString.append(IDConverter.get(data.gameID())).append(" |log ").append(data.gameName());
                gameString.append(" | White: ");
                if (data.whiteUsername() == null) {
                    gameString.append("available");
                } else {
                    gameString.append(data.whiteUsername());
                }
                gameString.append(" | Black: ");
                if (data.blackUsername() == null) {
                    gameString.append("available");
                } else {
                    gameString.append(data.blackUsername());
                }
                outString.append(gameString);
            }
            if (games.isEmpty()) {
                outString.append("\nLooks like there's nothing here yet!");
            }
        } catch (ServerFacadeException e) {
            if (e.getId() == 401) {
                return "Looks like you aren't authorized to do that. Please try again.";
            } else {
                return "Oops! Looks like something went wrong with logging out. Can you try again?";
            }
        }
        return outString.toString();
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
