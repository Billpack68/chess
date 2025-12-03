package websocket;

import websocket.messages.ServerMessage;

public class GameNotificationHandler implements ServerMessageObserver {
    @Override
    public void notify(ServerMessage message) {
        System.out.println("I'm TSA. We handle stuff. That's what we do. Consider this situation handled.");
    }
}