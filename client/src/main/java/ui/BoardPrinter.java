package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;


public class BoardPrinter {
    private String[] letters = {null, "a", "b", "c", "d", "e", "f", "g", "h"};

    public String printBoard(ChessBoard board, Boolean white) {
        StringBuilder printBoard = new StringBuilder();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                printBoard.append(getBackgroundColor(row, col)).append(getSquareText(row, col, board, white));
            }
            printBoard.append(RESET_BG_COLOR + "\n");
        }
        return printBoard.toString();
    }

    public String getBackgroundColor(int row, int col) {
        if (row == 0 || row == 9 || col == 0 || col == 9) {
            return SET_BG_COLOR_BLACK;
        }
        if (row % 2 == col % 2) {
            return SET_BG_COLOR_DARK_GREY;
        }
        return SET_BG_COLOR_DARKER_GREY;
    }

    public String getSquareText(int row, int col, ChessBoard board, Boolean white) {
        if (!white) {
            row = 9-row;
            col = 9-col;
        }
        if ((row == 0 && col == 0)
            || (row == 0 && col == 9)
            || (row == 9 && col == 0)
            || (row == 9 && col == 9)) {
            return "   ";
        }
        if (col == 0 || col == 9) {
            return SET_TEXT_COLOR_WHITE + " " + (9 - row) + " " + RESET_TEXT_COLOR;
        }
        if (row == 0 || row == 9) {
            return SET_TEXT_COLOR_WHITE + " " + letters[col] + " " + RESET_TEXT_COLOR;
        }
        ChessPiece pieceInSpot = board.getPiece(new ChessPosition(9-row, col));
        if (pieceInSpot != null) {
            StringBuilder pieceString = new StringBuilder();
            if (pieceInSpot.getTeamColor() == ChessGame.TeamColor.WHITE) {
                pieceString.append(SET_TEXT_COLOR_BLUE);
            } else {
                pieceString.append(SET_TEXT_COLOR_RED);
            }
            pieceString.append(getPieceText(pieceInSpot));
            pieceString.append(RESET_TEXT_COLOR);
            return pieceString.toString();
        }
        return "   ";
    }

    public String getPieceText (ChessPiece piece) {
        ChessPiece.PieceType type = piece.getPieceType();
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            if (type == ChessPiece.PieceType.KING) {
                return " K ";
            } else if (type == ChessPiece.PieceType.QUEEN) {
                return " Q ";
            } else if (type == ChessPiece.PieceType.BISHOP) {
                return " B ";
            } else if (type == ChessPiece.PieceType.KNIGHT) {
                return " N ";
            } else if (type == ChessPiece.PieceType.ROOK) {
                return " R ";
            } else {
                return " P ";
            }
        } else {
            if (type == ChessPiece.PieceType.KING) {
                return " k ";
            } else if (type == ChessPiece.PieceType.QUEEN) {
                return " q ";
            } else if (type == ChessPiece.PieceType.BISHOP) {
                return " b ";
            } else if (type == ChessPiece.PieceType.KNIGHT) {
                return " n ";
            } else if (type == ChessPiece.PieceType.ROOK) {
                return " r ";
            } else {
                return " p ";
            }
        }
    }
}
