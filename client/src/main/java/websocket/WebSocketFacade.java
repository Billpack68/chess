package websocket;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import jakarta.websocket.*;
import org.glassfish.tyrus.core.WebSocketException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;
    ServerMessageObserver serverMessageObserver;

    private final Gson gson = createSerializer();

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
                    ServerMessage clientMessage = gson.fromJson(message, ServerMessage.class);
                    if (clientMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
                        System.out.println("Invalid auth token, BUDDY!");
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

    public void sendTestMessage(String authToken, Integer gameID) throws WebSocketException {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.CONNECT, "BAD", gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new WebsocketException("Error with sending websocket message");
        }
    }

}