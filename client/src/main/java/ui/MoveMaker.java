package ui;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.List;

public class MoveMaker {
    private final List<String> validLetters = List.of("a", "b", "c", "d", "e", "f", "g", "h");

    public ChessPosition makePosition(String stringPosition) throws Exception {
        String letter;
        try {
            letter = stringPosition.substring(0, 1).toLowerCase();
        } catch (IndexOutOfBoundsException ex) {
            throw new Exception("Looks like you didn't format that position quite right.\nPlease try again.");
        }
        if (!validLetters.contains(letter)) {
            throw new Exception("Position must start with a, b, c, d, e, f, g, or h");
        }
        int col = validLetters.indexOf(letter) + 1;

        String integer;
        int row;
        try {
            integer = stringPosition.substring(1, 2);
            row = Integer.parseInt(integer);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw new Exception("Looks like you didn't format that position quite right.\nPlease try again");
        }

        return new ChessPosition(row, col);
    }

    public ChessMove makeMove(String... params) throws Exception {
        String start = params[0];
        String end = params[1];
        String promotion = null;
        if (params.length == 3) {
            promotion = params[2].toLowerCase();
        }

        ChessPosition startPosition = makePosition(start);
        ChessPosition endPosition = makePosition(end);
        ChessPiece.PieceType promotionType = null;
        if (promotion != null) {
            switch (promotion) {
                case "knight" -> promotionType = ChessPiece.PieceType.KNIGHT;
                case "rook" -> promotionType = ChessPiece.PieceType.ROOK;
                case "bishop" -> promotionType = ChessPiece.PieceType.BISHOP;
                case "queen" -> promotionType = ChessPiece.PieceType.QUEEN;
                default -> throw new Exception("Looks like that promotion type wasn't formatted correctly");
            }
        }
        return new ChessMove(startPosition, endPosition, promotionType);
    }
}
