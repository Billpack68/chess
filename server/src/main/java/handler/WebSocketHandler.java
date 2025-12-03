package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsContext;
import service.AuthService;
import service.InvalidAuthTokenException;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.sql.SQLException;

public class WebSocketHandler {
    private final AuthService authService;

    public WebSocketHandler() throws DataAccessException {
            authService = new AuthService(new AuthDAO());
    }

    public void handleConnect(WsContext ctx) {

        ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        String responseText = new Gson().toJson(response);
        ctx.send(responseText);
    }

    private boolean verifyAuth(String authToken) {
        try {
            authService.getAuth(authToken);
        } catch (SQLException | DataAccessException | InvalidAuthTokenException e) {
            return false;
        }
        return true;
    }

    private void sendUnauthorized(WsContext ctx) {
        ErrorMessage response = new ErrorMessage(ServerMessage.ServerMessageType.ERROR,
                "Error: Invalid auth token");
        String responseText = new Gson().toJson(response);
        ctx.send(responseText);
    }
}
