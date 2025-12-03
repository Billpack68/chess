package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsContext;
import model.AuthData;
import service.AuthService;
import service.InvalidAuthTokenException;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class WebSocketHandler {
    private final AuthService authService;
    private Map<Integer, ArrayList<WsContext>> players;
    private Map<Integer, ArrayList<WsContext>> observers;

    public WebSocketHandler() throws DataAccessException {
            authService = new AuthService(new AuthDAO());
    }

    public void handleConnect(WsContext ctx, String authToken) {
        AuthData senderAuthData = verifyAuth(authToken);
        if (senderAuthData != null) {
            ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            String responseText = new Gson().toJson(response);
            ctx.send(responseText);
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
