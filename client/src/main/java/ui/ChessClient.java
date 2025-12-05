package ui;

import chess.ChessMove;
import chess.ChessPosition;
import model.GameData;
import requests.*;
import results.JoinGameResult;
import results.ListGamesResult;
import websocket.GameNotificationHandler;
import websocket.ObserveNotificationHandler;
import websocket.WebSocketFacade;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;


import java.util.*;

public class ChessClient {
    private String clientName;
    private String authToken;
    private final ServerFacade server;
    private String serverUrl;
    private State state = State.SIGNED_OUT;
    private Map<Integer, Integer> iDConverter = new HashMap<>();
    private Map<Integer, Integer> iDUnConverter = new HashMap<>();
    private BoardPrinter boardPrinter = new BoardPrinter();
    private Map<Integer, GameData> gameData = new HashMap<>();
    private WebSocketFacade webSocketFacade;
    private MoveMaker moveMaker = new MoveMaker();
    private Integer inGameID;

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
            case "move" -> move(params);
            case "leave" -> leave(params);
            case "resign" -> resign(params);
            case "quit" -> "quit";
            default -> help();
        };

    }

    private void printPrompt() {
        if (state == State.SIGNED_IN) {
            System.out.print("\nLogged in as " + clientName + " >>> ");
        } else if (state == State.SIGNED_OUT) {
            System.out.print("\n>>> ");
        } else if (state == State.IN_GAME) {
            System.out.print("\nIn game as " + clientName + " >>> ");
        } else if (state == State.OBSERVING) {
            System.out.print("\nObserving as " + clientName + " >>> ");
        }
    }

    private String help() {
        if (state == State.SIGNED_OUT) {
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
        } else if (state == State.SIGNED_IN) {
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
        } else if (state == State.IN_GAME) {
            return """
                    Commands:
                    redraw
                        Redraw the chess board
                    move [start-position] [end-position] [optional: promotion piece type]
                        Move the piece at the start position to the end position
                        Positions should be in the format "b4" or similar, not "4,4"
                        Promotion piece type should be "rook" "knight" "bishop" or "queen"
                    highlight [position]
                        Highlight the legal moves a piece can make
                    resign
                        Resign the game. The game will be over
                    leave
                        Leave the game
                    help
                        Shows a list of commands you can do
                    quit
                        Exits the program
                    """;
        } else {
            return """
                    Commands:
                    redraw
                        Redraw the chess board
                    highlight [position]
                        Highlight the legal moves a piece can make
                    leave
                        Leave the game
                    help
                        Shows a list of commands you can do
                    quit
                        Exits the program
                    """;
        }
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
        state = State.SIGNED_IN;
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
        state = State.SIGNED_OUT;
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
            inGameID = iDUnConverter.get(gameID);
            JoinGameRequest request = new JoinGameRequest(authToken, playerColor, inGameID);
            JoinGameResult result = server.joinGame(request);
            if (playerColor.equals("WHITE")) {
                webSocketFacade = new WebSocketFacade(serverUrl, new GameNotificationHandler(clientName), true);
                webSocketFacade.sendConnectMessage(authToken, inGameID, ConnectCommand.JoinType.WHITE);
            } else {
                webSocketFacade = new WebSocketFacade(serverUrl, new GameNotificationHandler(clientName), false);
                webSocketFacade.sendConnectMessage(authToken, inGameID, ConnectCommand.JoinType.BLACK);
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
        state = State.IN_GAME;

        return help();
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
        state = State.OBSERVING;
        inGameID = iDUnConverter.get(gameID);
        try {
            webSocketFacade = new WebSocketFacade(serverUrl, new ObserveNotificationHandler(clientName),
                    true);
            webSocketFacade.sendConnectMessage(authToken, inGameID, ConnectCommand.JoinType.OBSERVER);
        } catch (Exception e) {
            return e.getMessage();
        }

        return help();
    }

    private String move(String... params) {
        try {
            assertInGame();
        } catch (Exception ex) {
            return "You must be playing a game to use that command";
        }

        if (params.length < 2 || params.length > 3) {
            return "Expected: move [start-position] [end-position] [optional: promotion piece]";
        }

        ChessMove move;
        try {
            move = moveMaker.makeMove(params);
        } catch (Exception e) {
            return e.getMessage();
        }

        webSocketFacade.sendMakeMoveMessage(authToken, inGameID, move);

        return "";
    }

    private String leave(String... params) {
        try {
            assertInGameMode();
        } catch (Exception ex) {
            return "You must be playing a game to use that command";
        }
        if (params.length > 0) {
            return "Expected: leave";
        }
        webSocketFacade.sendLeaveMessage(authToken, inGameID);
        inGameID = null;
        state = State.SIGNED_IN;
        return "";
    }

    private String resign(String... params) {
        try {
            assertInGame();
        } catch (Exception e) {
            return "You have to be playing a game to use that command";
        }
        System.out.println("Are you sure? [Y/N]");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        if (!line.equalsIgnoreCase("Y")) {return "Command canceled";}
        webSocketFacade.sendMessage(UserGameCommand.CommandType.RESIGN, authToken, inGameID);
        return "Let's do this :)";
    }
    private void assertSignedIn() throws Exception {
        if (state == State.SIGNED_OUT) {
            throw new Exception("You must sign in first");
        } else if (state == State.IN_GAME || state == State.OBSERVING) {
            throw new Exception("You cannot do that while in a game");
        }
    }
    private void assertNotSignedIn() throws Exception {
        if (state != State.SIGNED_OUT) {
            throw new Exception("You must be signed out to use that command");
        }
    }
    private void assertInGame() throws Exception {
        if (state != State.IN_GAME) {
            throw new Exception("You must be in a game to use that command");
        }
    }
    private void assertInGameMode() throws Exception {
        if (state != State.IN_GAME && state != State.OBSERVING) {
            throw new Exception("You must be in a game to use that command");
        }
    }
}
