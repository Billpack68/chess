package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;


public class BoardPrinter {
    private final String[] letters = {null, "a", "b", "c", "d", "e", "f", "g", "h"};

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

    public String getBackgroundColorHighlight(int row, int col, Set<ChessPosition> positions, boolean white,
                                              boolean pieceWhite) {
        if (row == 0 || row == 9 || col == 0 || col == 9) {
            return SET_BG_COLOR_BLACK;
        }

        boolean lightAndDark = (row % 2 == col % 2);
        if (!white) {
            lightAndDark = !lightAndDark;
        }

        String darkBG;
        String lightBG;
        if (!pieceWhite) {
            darkBG = SET_BG_COLOR_RED;
            lightBG =SET_BG_COLOR_DARK_RED;
        } else {
            darkBG = SET_BG_COLOR_BLUE;
            lightBG = SET_BG_COLOR_DARK_BLUE;
        }
        if (white && positions.contains(new ChessPosition(9-row, col))) {
            return lightAndDark ? darkBG : lightBG;
        }
        else if (!white && positions.contains(new ChessPosition(row, 9-col))) {
            return !lightAndDark ? darkBG : lightBG;
        }

        if (white) {
            return lightAndDark ? SET_BG_COLOR_DARK_GREY : SET_BG_COLOR_DARKER_GREY;
        }
        return !lightAndDark ? SET_BG_COLOR_DARK_GREY : SET_BG_COLOR_DARKER_GREY;
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

    public String highlight(ChessGame game, Boolean white, ChessPosition position) {
        ChessPiece pieceInSpot = game.getBoard().getPiece(position);
        ChessBoard board = game.getBoard();
        if (pieceInSpot == null) {
            return printBoard(board, white);
        }
        ChessGame.TeamColor color = pieceInSpot.getTeamColor();
        boolean pieceWhite = false;
        if (color == ChessGame.TeamColor.WHITE) {
            pieceWhite = true;
        }
        Collection<ChessMove> validMoves = game.validMoves(position);
        Set<ChessPosition> validSquares = new HashSet<>();
        for (ChessMove move : validMoves) {
            validSquares.add(move.getEndPosition());
        }
        StringBuilder printBoard = new StringBuilder();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                printBoard.append(getBackgroundColorHighlight(row, col, validSquares, white, pieceWhite))
                        .append(getSquareText(row, col, board, white));
            }
            printBoard.append(RESET_BG_COLOR + "\n");
        }
        return printBoard.toString();
    }
}
