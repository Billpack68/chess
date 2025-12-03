package websocket.messages;

public class LoadGameMessage extends ServerMessage{
    private final Object game;

    public LoadGameMessage(ServerMessageType type, Object game) {
        super(type);
        this.game = game;
    }

    public Object getGame() {
        return game;
    }
}
