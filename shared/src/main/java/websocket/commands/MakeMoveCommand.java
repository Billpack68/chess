package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;
    private final ChessGame.TeamColor senderColor;

    public MakeMoveCommand (String authToken, Integer gameID, ChessMove move, ChessGame.TeamColor senderColor) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
        this.senderColor = senderColor;
    }

    public ChessMove getMove() {
        return move;
    }

    public ChessGame.TeamColor getSenderColor() {
        return senderColor;
    }
}
