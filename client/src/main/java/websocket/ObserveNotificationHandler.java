package websocket;

import websocket.messages.ServerMessage;

public class ObserveNotificationHandler implements ServerMessageObserver {
    private final String clientName;

    public ObserveNotificationHandler(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public void notify(ServerMessage message) {
        System.out.print("\nObserving as " + clientName + " >>> ");
    }
}