package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsContext;
import model.AuthData;
import service.AuthService;
import service.InvalidAuthTokenException;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebSocketHandler {
    private final AuthService authService;
    private Map<Integer, ArrayList<WsContext>> gamers = new HashMap<>();

    public WebSocketHandler() throws DataAccessException {
            authService = new AuthService(new AuthDAO());
    }

    public void handleConnect(WsContext ctx, String authToken, Integer gameID) {
        AuthData senderAuthData = verifyAuth(authToken);
        if (senderAuthData != null) {
            if (!gamers.containsKey(gameID)) {
                gamers.put(gameID, new ArrayList<>());
            }
            gamers.get(gameID).add(ctx);

            for (WsContext storedCTX : gamers.get(gameID)) {
                if (!storedCTX.equals(ctx)) {
                    ServerMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                            "\n" + senderAuthData.username() + " joined the game!");
                    String notificationJson = new Gson().toJson(notification);
                    storedCTX.send(notificationJson);
                }
            }

        } else {
            sendUnauthorized(ctx);
        }
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

    private void sendUnauthorized(WsContext ctx) {
        ErrorMessage response = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                "Error: Unable to authorize user");
        String responseText = new Gson().toJson(response);
        ctx.send(responseText);
    }
}
