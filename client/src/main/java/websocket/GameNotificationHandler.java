package websocket;

import websocket.messages.ServerMessage;

public class GameNotificationHandler implements ServerMessageObserver {
    private final String clientName;

    public GameNotificationHandler(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public void notify(ServerMessage message) {
        System.out.println("Websocket message received");
        System.out.print("\nIn game as " + clientName + " >>> ");
    }
}