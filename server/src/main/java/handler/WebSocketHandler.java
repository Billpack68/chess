package handler;

import chess.*;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import service.AuthService;
import service.InvalidAuthTokenException;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.*;

public class WebSocketHandler {
    private final AuthService authService;
    private final GameDAO gameDAO = new GameDAO();
    private Map<Integer, ArrayList<WsContext>> gamers = new HashMap<>();
    private final String[] letters = {null, "a", "b", "c", "d", "e", "f", "g", "h"};

    public WebSocketHandler() throws DataAccessException {
        authService = new AuthService(new AuthDAO());
    }

    public void handleConnect(WsContext ctx, String authToken, Integer gameID, ConnectCommand.JoinType joinType) {
        AuthData senderAuthData = verifyAuth(authToken);
        if (senderAuthData != null) {
            GameData game = null;
            try {
                game = gameDAO.findGameDataByID(gameID);
            } catch (DataAccessException e) {
                sendErrorMessage(ctx, "Error: unable to connect to database");
            }
            if (game == null) {
                sendErrorMessage(ctx, "Error: invalid game ID");
                return;
            }
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
        ChessPiece pieceInSpot = game.getBoard().getPiece(move.getStartPosition());
        if (pieceInSpot != null) {
            ChessGame.TeamColor pieceColor = pieceInSpot.getTeamColor();
            if (pieceColor == ChessGame.TeamColor.WHITE &&
                    !Objects.equals(gameData.whiteUsername(), senderAuthData.username())) {
                sendErrorMessage(ctx, "Error: You can only move pieces on your team");
                return;
            } else if (pieceColor == ChessGame.TeamColor.BLACK &&
                    !Objects.equals(gameData.blackUsername(), senderAuthData.username())) {
                sendErrorMessage(ctx, "Error: You can only move pieces on your team");
                return;
            }
        }
        if (game.isGameOver()) {
            sendErrorMessage(ctx, "Error: This game is over");
            return;
        }
        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            sendErrorMessage(ctx, "Error: Looks like that move isn't valid");
            return;
        }

        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), game);
        try {
            gameDAO.updateGame(gameData, newGameData);
        } catch (DataAccessException ex) {
            sendErrorMessage(ctx, "Error: we couldn't access the database.");
            return;
        }

        for (WsContext storedCTX : gamers.get(gameID)) {
            sendLoadGame(gameID, storedCTX);
            if (!storedCTX.equals(ctx)) {
                ServerMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        senderAuthData.username() + getMoveText(move));
                String notificationJson = new Gson().toJson(notification);
                storedCTX.send(notificationJson);
            }
        }

        ChessGame.TeamColor enemyColor;
        String enemyUsername;
        assert pieceInSpot != null;
        if (pieceInSpot.getTeamColor() == ChessGame.TeamColor.WHITE) {
            enemyColor = ChessGame.TeamColor.BLACK;
            enemyUsername = gameData.blackUsername();
        } else {
            enemyColor = ChessGame.TeamColor.WHITE;
            enemyUsername = gameData.whiteUsername();
        }

        boolean isInCheckmate = game.isInCheckmate(enemyColor);
        boolean isInCheck = !isInCheckmate && game.isInCheck(enemyColor);
        boolean isInStalemate = !isInCheckmate && !isInCheck && game.isInStalemate(enemyColor);

        if (isInCheckmate || isInCheck || isInStalemate) {
            if (isInCheckmate || isInStalemate) {
                game.gameIsOver();
                GameData newerGameData = new GameData(newGameData.gameID(), newGameData.whiteUsername(),
                        newGameData.blackUsername(), newGameData.gameName(), game);
                try {
                    gameDAO.updateGame(newGameData, newerGameData);
                } catch (DataAccessException ex) {
                    sendErrorMessage(ctx, "Error: we couldn't access the database.");
                    return;
                }
            }
            for (WsContext storedCTX : gamers.get(gameID)) {
                ServerMessage notification = getServerMessage(isInCheckmate, isInCheck, senderAuthData, enemyUsername);
                String notificationJson = new Gson().toJson(notification);
                storedCTX.send(notificationJson);
            }
        }

    }

    public void handleLeave(WsContext ctx, String authToken, Integer gameID) {
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

        if (Objects.equals(senderAuthData.username(), gameData.whiteUsername())) {
            GameData newGameData = new GameData(gameData.gameID(), null, gameData.blackUsername(),
                    gameData.gameName(), gameData.game());
            try {
                gameDAO.updateGame(gameData, newGameData);
            } catch (DataAccessException e) {
                sendErrorMessage(ctx, "Error: we couldn't access the database");
                return;
            }
        } else if (Objects.equals(senderAuthData.username(), gameData.blackUsername())) {
            GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null,
                    gameData.gameName(), gameData.game());
            try {
                gameDAO.updateGame(gameData, newGameData);
            } catch (DataAccessException e) {
                sendErrorMessage(ctx, "Error: we couldn't access the database");
                return;
            }
        }

        gamers.get(gameID).remove(ctx);

        for (WsContext storedCTX : gamers.get(gameID)) {
            ServerMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    senderAuthData.username() + " left the game");
            String notificationJson = new Gson().toJson(notification);
            storedCTX.send(notificationJson);
        }
    }

    public void handleResign(WsMessageContext ctx, String authToken, Integer gameID) {
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
        game.gameIsOver();

        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), game);
        try {
            gameDAO.updateGame(gameData, newGameData);
        } catch (DataAccessException e) {
            sendErrorMessage(ctx, "Error: we couldn't access the database");
            return;
        }

        for (WsContext storedCTX : gamers.get(gameID)) {
            ServerMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    senderAuthData.username() + " resigned from the game");
            String notificationJson = new Gson().toJson(notification);
            storedCTX.send(notificationJson);
        }
    }

    private static ServerMessage getServerMessage(boolean isInCheckmate, boolean isInCheck, AuthData senderAuthData,
                                                  String enemyUsername) {
        ServerMessage notification;
        if (isInCheckmate) {
            notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    senderAuthData.username() + " put " + enemyUsername + " in checkmate!");
        } else if (isInCheck) {
            notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    senderAuthData.username() + " put " + enemyUsername + " in check!");
        } else {
            notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    senderAuthData.username() + " put " + enemyUsername + " in stalemate!");
        }
        return notification;
    }

    private String getMoveText(ChessMove move) {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        String returnString = "";
        returnString += " moved their piece at " + letters[startPosition.getColumn()] + startPosition.getRow();
        returnString += " to " + letters[endPosition.getColumn()] + endPosition.getRow();
        if (move.getPromotionPiece() != null) {
            ChessPiece.PieceType type = move.getPromotionPiece();
            returnString += " and promoted it to a ";
            switch (type) {
                case ChessPiece.PieceType.QUEEN -> returnString += "queen";
                case ChessPiece.PieceType.ROOK -> returnString += "rook";
                case ChessPiece.PieceType.KNIGHT -> returnString += "knight";
                case ChessPiece.PieceType.BISHOP -> returnString += "bishop";
            }
        }
        return returnString;
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
