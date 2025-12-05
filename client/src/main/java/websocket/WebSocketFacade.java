package websocket;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import jakarta.websocket.*;
import org.glassfish.tyrus.core.WebSocketException;
import ui.BoardPrinter;
import websocket.commands.ConnectCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class WebSocketFacade extends Endpoint {

    Session session;
    ServerMessageObserver serverMessageObserver;
    private GameStorage gameStorage = new GameStorage(null);
    BoardPrinter printer = new BoardPrinter();
    boolean clientWhite;
    private final List<String> validLetters = List.of("a", "b", "c", "d", "e", "f", "g", "h");


    private final Gson gson = createSerializer();

    public WebSocketFacade(String url, ServerMessageObserver serverMessageObserver,
                           boolean clientWhite) throws WebsocketException {
        this.clientWhite = clientWhite;
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.serverMessageObserver = serverMessageObserver;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage clientMessage = gson.fromJson(message, ServerMessage.class);
                    if (clientMessage instanceof NotificationMessage) {
                        System.out.println(((NotificationMessage) clientMessage).getMessage());
                    } else if (clientMessage instanceof LoadGameMessage) {
                        ChessGame game = ((LoadGameMessage) clientMessage).getGame();
                        updateStoredGame(game);
                    } else if (clientMessage instanceof ErrorMessage) {
                        System.out.println(((ErrorMessage) clientMessage).getErrorMessage());
                    }
                    serverMessageObserver.notify(clientMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new WebsocketException("Error: Couldn't establish websocket connection");
        }
    }

    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        Gson defaultGson = new Gson();

        gsonBuilder.registerTypeAdapter(ServerMessage.class,
                (JsonDeserializer<ServerMessage>) (el, type, ctx) -> {
                    ServerMessage message = null;
                    if (el.isJsonObject()) {
                        String messageType = el.getAsJsonObject().get("serverMessageType").getAsString();
                        switch (ServerMessage.ServerMessageType.valueOf(messageType)) {
                            case NOTIFICATION -> message = defaultGson.fromJson(el, NotificationMessage.class);
                            case ERROR -> message = defaultGson.fromJson(el, ErrorMessage.class);
                            case LOAD_GAME -> message = defaultGson.fromJson(el, LoadGameMessage.class);
                        }
                    }
                    return message;
                });

        return gsonBuilder.create();
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void sendMessage(UserGameCommand.CommandType type, String authToken, Integer gameID)
            throws WebSocketException {
        try {
            var action = new UserGameCommand(type, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new WebsocketException("Error with sending websocket message");
        }
    }

    public void sendConnectMessage(String authToken, Integer gameID, ConnectCommand.JoinType joinType)
            throws WebSocketException {
        try {
            var action = new ConnectCommand(authToken, gameID, joinType);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new WebsocketException("Error with sending websocket message");
        }
    }

    public void sendMakeMoveMessage(String authToken, Integer gameID, ChessMove move) {
        try {
            var action = new MakeMoveCommand(null, null, null);
            if (clientWhite) {
                action = new MakeMoveCommand(authToken, gameID, move);
            } else {
                action = new MakeMoveCommand(authToken, gameID, move);
            }
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new WebsocketException("Error with sending websocket message");
        }
    }

    public void sendLeaveMessage(String authToken, Integer gameID) {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new WebsocketException("Error with sending websocket message");
        }
    }

    public String getBoard() {
        return printer.printBoard(gameStorage.getGame().getBoard(), clientWhite);
    }

    public String highlight(String... params) throws Exception {
        String letter;
        try {
            letter = params[0].substring(0, 1).toLowerCase();
        } catch (IndexOutOfBoundsException ex) {
            throw new Exception("Looks like you didn't format that position quite right.\nPlease try again.");
        }
        if (!validLetters.contains(letter)) {
            throw new Exception("Position must start with a, b, c, d, e, f, g, or h");
        }
        int col = validLetters.indexOf(letter) + 1;

        String integer;
        int row;
        try {
            integer = params[0].substring(1, 2);
            row = Integer.parseInt(integer);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw new Exception("Looks like you didn't format that position quite right.\nPlease try again");
        }
        return printer.highlight(gameStorage.getGame(), clientWhite, new ChessPosition(row, col));
    }

    private void updateStoredGame(ChessGame game) {
        gameStorage.updateGame(game, clientWhite);
    }

}