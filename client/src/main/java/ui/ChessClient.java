package ui;

import chess.ChessBoard;
import model.GameData;
import requests.*;
import results.JoinGameResult;
import results.ListGamesResult;
import websocket.GameNotificationHandler;
import websocket.WebSocketFacade;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;


import java.util.*;

public class ChessClient {
    private String clientName;
    private String authToken;
    private final ServerFacade server;
    private String serverUrl;
    private State state = State.SIGNEDOUT;
    private Map<Integer, Integer> iDConverter = new HashMap<>();
    private Map<Integer, Integer> iDUnConverter = new HashMap<>();
    private BoardPrinter boardPrinter = new BoardPrinter();
    private Map<Integer, GameData> gameData = new HashMap<>();
    private WebSocketFacade webSocketFacade;

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to the CS240 Chess client! Here's a list of commands to get you started:");
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
            case "join" -> joinGame(params);
            case "observe" -> observe(params);
            case "test" -> test(params);
            case "quit" -> "quit";
            default -> help();
        };

    }

    private void printPrompt() {
        if (state == State.SIGNEDIN) {
            System.out.print("\nLogged in as " + clientName + " >>> ");
        } else if (state == State.SIGNEDOUT) {
            System.out.print("\n>>> ");
        } else if (state == State.INGAME) {
            System.out.print("\nIn game as " + clientName + " >>> ");
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
                    return "Sorry, that username or email is already in use";
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
            List<GameData> games = new ArrayList<>(result.games());
            games.sort(Comparator.comparingInt(GameData::gameID));

            for (GameData data : games) {
                if (!iDConverter.containsKey(data.gameID())) {
                    iDConverter.put(data.gameID(), iDConverter.size()+1);
                    gameData.put(iDConverter.size()+1, data);
                }
                if (!iDUnConverter.containsKey(iDConverter.get(data.gameID()))){
                    iDUnConverter.put(iDUnConverter.size()+1, data.gameID());
                }
                StringBuilder gameString = new StringBuilder("\n");
                gameString.append(iDConverter.get(data.gameID())).append(" | ").append(data.gameName());
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

    private String joinGame(String... params) {
        try {
            assertSignedIn();
        } catch (Exception e) {
            return e.getMessage();
        }
        if (params.length != 2) {
            return "Expected: join [game-id] [color]";
        }
        listGames(); // To update the list of Game IDs, in case they created a game and then run this command
        Integer gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Please provide a number for the gameID (Like `1` or `2`, not `one` or `two`)";
        }
        String playerColor = params[1].toUpperCase();
        if (!iDUnConverter.containsKey(gameID)) {
            return "Looks like that game doesn't exist. Please try again.";
        }
        if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
            return "Please input `white` or `black` for the color";
        }
        try {
            JoinGameRequest request = new JoinGameRequest(authToken, playerColor, iDUnConverter.get(gameID));
            JoinGameResult result = server.joinGame(request);
            webSocketFacade = new WebSocketFacade(serverUrl, new GameNotificationHandler(clientName));
            if (playerColor.equals("WHITE")) {
                webSocketFacade.sendConnectMessage(authToken, iDUnConverter.get(gameID), ConnectCommand.JoinType.WHITE);
            } else {
                webSocketFacade.sendConnectMessage(authToken, iDUnConverter.get(gameID), ConnectCommand.JoinType.BLACK);
            }

        } catch (ServerFacadeException e) {
            if (e.getId() == 400) {
                return "Expected: join [game-id] [color]";
            } else if (e.getId() == 401) {
                return "Looks like you aren't authorized to do that. Please try logging out and back in.";
            } else if (e.getId() ==  403) {
                return "That color is taken. Please try a different color or a different game";
            } else {
                return "Oops! Looks like something went wrong with logging out. Can you try again?";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        state = State.INGAME;

//        //Filler code for phase 5 - print a default board
//        ChessBoard newBoard = new ChessBoard();
//        newBoard.resetBoard();
//
//        if (playerColor.equals("WHITE")){
//            return boardPrinter.printBoard(newBoard, true);
//        }
//        return boardPrinter.printBoard(newBoard, false);
        return "";
    }

    private String observe(String... params) {
        try {
            assertSignedIn();
        } catch (Exception e) {
            return e.getMessage();
        }
        if (params.length != 1) {
            return "Expected: observe [game-id]";
        }
        Integer gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "Please provide a number for the gameID";
        }
        listGames(); // update the list of games created in case they created one and haven't called listgames
        if (!iDUnConverter.containsKey(gameID)) {
            return "Looks like that game doesn't exist. Please try again.";
        }
        state = State.INGAME;

        try {
            webSocketFacade = new WebSocketFacade(serverUrl, new GameNotificationHandler(clientName));
            webSocketFacade.sendConnectMessage(authToken, iDUnConverter.get(gameID), ConnectCommand.JoinType.OBSERVER);
        } catch (Exception e) {
            return e.getMessage();
        }

//        //Filler code for phase 5 - print a default board
//        ChessBoard newBoard = new ChessBoard();
//        newBoard.resetBoard();
//        return boardPrinter.printBoard(newBoard, true);
        return "";
    }

    private String test(String... params) {
        try {
            assertInGame();
        } catch (Exception e) {
            return e.getMessage();
        }
        if (params.length != 0) {
            return "Expected: test";
        }
        return "Tested";
    }

    private void assertSignedIn() throws Exception {
        if (state == State.SIGNEDOUT) {
            throw new Exception("You must sign in first");
        } else if (state == State.INGAME) {
            throw new Exception("You cannot do that while in a game");
        }
    }

    private void assertNotSignedIn() throws Exception {
        if (state != State.SIGNEDOUT) {
            throw new Exception("You must be signed out to use that command");
        }
    }

    private void assertInGame() throws Exception {
        if (state != State.INGAME) {
            throw new Exception("You must be in a game to use that command");
        }
    }
}
