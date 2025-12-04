package handler;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import service.AuthService;
import service.InvalidAuthTokenException;
import websocket.commands.ConnectCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WebSocketHandler {
    private final AuthService authService;
    private final GameDAO gameDAO = new GameDAO();
    private Map<Integer, ArrayList<WsContext>> gamers = new HashMap<>();

    public WebSocketHandler() throws DataAccessException {
        authService = new AuthService(new AuthDAO());
    }

    public void handleConnect(WsContext ctx, String authToken, Integer gameID, ConnectCommand.JoinType joinType) {
        AuthData senderAuthData = verifyAuth(authToken);
        if (senderAuthData != null) {
            if (!gamers.containsKey(gameID)) {
                gamers.put(gameID, new ArrayList<>());
            }
            gamers.get(gameID).add(ctx);

            for (WsContext storedCTX : gamers.get(gameID)) {
                if (!storedCTX.equals(ctx)) {
                    ServerMessage notification = getJoinMessage(joinType, senderAuthData);
                    String notificationJson = new Gson().toJson(notification);
                    storedCTX.send(notificationJson);
                }
            }
            sendLoadGame(gameID, ctx);

        } else {
            sendErrorMessage(ctx, "Error: unable to authenticate user");
        }
    }

    public void handleMakeMove(WsContext ctx, String authToken, Integer gameID, ChessMove move) {
        AuthData senderAuthData = verifyAuth(authToken);
        if (senderAuthData == null) {
            sendErrorMessage(ctx, "Error: unable to authenticate user");
            return;
        }
        GameData gameData = null;
        try {
            gameData = gameDAO.findGameDataByID(gameID);
        } catch (DataAccessException e) {
            sendErrorMessage(ctx, "Error: we couldn't find that game in our database");
            return;
        }
        ChessGame game = gameData.game();
        Collection<ChessMove> validMoves = game.validMoves(move.getStartPosition());
        ChessGame.TeamColor moveColor = game.getBoard().getPiece(move.getStartPosition()).getTeamColor();
        if (game.getTeamTurn() != moveColor || !validMoves.contains(move)) {
            sendErrorMessage(ctx, "Error: Looks like that move isn't valid");
        }
    }

    private static ServerMessage getJoinMessage(ConnectCommand.JoinType joinType, AuthData senderAuthData) {
        ServerMessage notification;
        if (joinType == ConnectCommand.JoinType.WHITE) {
            notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    "\n" + senderAuthData.username() + " joined the game as white!");
        } else if (joinType == ConnectCommand.JoinType.BLACK) {
            notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    "\n" + senderAuthData.username() + " joined the game as black!");
        } else {
            notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    "\n" + senderAuthData.username() + " joined the game as an observer!");
        }
        return notification;
    }

    private AuthData verifyAuth(String authToken) {
        AuthData senderAuthData = null;
        try {
            senderAuthData = authService.getAuth(authToken);
        } catch (SQLException | DataAccessException | InvalidAuthTokenException e) {
            return null;
        }
        return senderAuthData;
    }

    private void sendLoadGame(Integer gameID, WsContext ctx) {
        GameData gameData = null;
        try {
            gameData = gameDAO.findGameDataByID(gameID);
        } catch (DataAccessException e) {
            sendErrorMessage(ctx, "Error: we couldn't find that game in our database");
            return;
        }
        ChessGame game = gameData.game();
        LoadGameMessage response = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME,
                game);
        String responseText = new Gson().toJson(response);
        ctx.send(responseText);
    }

    private void sendErrorMessage(WsContext ctx, String message) {
        ErrorMessage response = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                message);
        String responseText = new Gson().toJson(response);
        ctx.send(responseText);
    }
}
