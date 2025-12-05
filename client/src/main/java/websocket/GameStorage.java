package websocket;

import chess.ChessBoard;
import chess.ChessGame;
import ui.BoardPrinter;

import static ui.EscapeSequences.ERASE_SCREEN;

public class GameStorage {
    private ChessGame game = null;
    private BoardPrinter printer = new BoardPrinter();

    public GameStorage(ChessGame game) {
        this.game = game;
    }

    public ChessGame getGame() {
        return game;
    }

    public void updateGame(ChessGame game, boolean white) {
        this.game = game;
        ChessBoard board = game.getBoard();
        System.out.println("\n" + printer.printBoard(board, white));
    }
}
