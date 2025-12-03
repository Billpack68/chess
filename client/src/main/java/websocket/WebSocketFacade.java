package websocket;

import com.google.gson.Gson;

import jakarta.websocket.*;
import org.glassfish.tyrus.core.WebSocketException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;
    ServerMessageObserver serverMessageObserver;

    public WebSocketFacade(String url, ServerMessageObserver serverMessageObserver) throws WebsocketException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.serverMessageObserver = serverMessageObserver;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage clientMessage = new Gson().fromJson(message, ServerMessage.class);
                    serverMessageObserver.notify(clientMessage);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new WebsocketException("Error: Couldn't establish websocket connection");
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void sendTestMessage(String authToken, Integer gameID) throws WebSocketException {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new WebsocketException("Error with sending websocket message");
        }
    }

}