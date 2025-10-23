package handler;

public class CreateJoinGameRequest {
    public String playerColor;
    public int gameID;

    public CreateJoinGameRequest() {}

    public CreateJoinGameRequest(String playerColor, int gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public int getGameID() {
        return gameID;
    }
}