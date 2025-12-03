package websocket.commands;

import chess.ChessMove;

public class ConnectCommand extends UserGameCommand {

    private JoinType joinType;

    public enum JoinType {
        WHITE,
        BLACK,
        OBSERVER
    }

    public ConnectCommand (String authToken, Integer gameID, JoinType type) {
        super(CommandType.CONNECT, authToken, gameID);
        this.joinType = type;
    }

    public JoinType getJoinType() {
        return joinType;
    }
}