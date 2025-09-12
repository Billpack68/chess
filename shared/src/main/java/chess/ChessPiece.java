package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private PieceType type;
    private ChessGame.TeamColor color;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() { return color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return type == that.type && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color);
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        List<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        if (piece.getPieceType() == PieceType.BISHOP) {
            boolean checkingSW = true;
            boolean checkingNW = true;
            boolean checkingSE = true;
            boolean checkingNE = true;

            int checkingRow = row;
            int checkingCol = col;
            while (checkingSW) {
                checkingRow -= 1;
                checkingCol -= 1;
                ChessPosition checkingPosition = new ChessPosition(checkingRow, checkingCol);
                if (checkingRow == 0 || checkingCol == 0) {
                    checkingSW = false;
                } else {
                    ChessPiece pieceInSpot = board.getPiece(checkingPosition);
                    if (pieceInSpot == null || pieceInSpot.getTeamColor() != color) {
                        possibleMoves.add(new ChessMove(myPosition, checkingPosition, null));
                    }
                    if (pieceInSpot != null) {
                        checkingSW = false;
                    }
                }
            }

            checkingRow = row;
            checkingCol = col;
            while (checkingNW) {
                checkingRow += 1;
                checkingCol -= 1;
                ChessPosition checkingPosition = new ChessPosition(checkingRow, checkingCol);
                if (checkingRow == 9 || checkingCol == 0) {
                    checkingNW = false;
                } else {
                    ChessPiece pieceInSpot = board.getPiece(checkingPosition);
                    if (pieceInSpot == null || pieceInSpot.getTeamColor() != color) {
                        possibleMoves.add(new ChessMove(myPosition, checkingPosition, null));
                    }
                    if (pieceInSpot != null) {
                        checkingNW = false;
                    }
                }
            }

            checkingRow = row;
            checkingCol = col;
            while (checkingNE) {
                checkingRow += 1;
                checkingCol += 1;
                ChessPosition checkingPosition = new ChessPosition(checkingRow, checkingCol);
                if (checkingRow == 9 || checkingCol == 9) {
                    checkingNE = false;
                } else {
                    ChessPiece pieceInSpot = board.getPiece(checkingPosition);
                    if (pieceInSpot == null || pieceInSpot.getTeamColor() != color) {
                        possibleMoves.add(new ChessMove(myPosition, checkingPosition, null));
                    }
                    if (pieceInSpot != null) {
                        checkingNE = false;
                    }
                }
            }

            checkingRow = row;
            checkingCol = col;
            while (checkingSE) {
                checkingRow -= 1;
                checkingCol += 1;
                ChessPosition checkingPosition = new ChessPosition(checkingRow, checkingCol);
                if (checkingRow == 0 || checkingCol == 9) {
                    checkingSE = false;
                } else {
                    ChessPiece pieceInSpot = board.getPiece(checkingPosition);
                    if (pieceInSpot == null || pieceInSpot.getTeamColor() != color) {
                        possibleMoves.add(new ChessMove(myPosition, checkingPosition, null));
                    }
                    if (pieceInSpot != null) {
                        checkingSE = false;
                    }
                }
            }
        }

        return possibleMoves;
    }
}
