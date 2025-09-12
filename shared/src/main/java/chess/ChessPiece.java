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
            int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            for (int i = 0; i < directions.length; i++) {
                walk(row, col, directions[i][0], directions[i][1], possibleMoves, board, myPosition, 7);
            }
        } else if (piece.getPieceType() == PieceType.ROOK) {
            int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            for (int i = 0; i < directions.length; i++) {
                walk(row, col, directions[i][0], directions[i][1], possibleMoves, board, myPosition, 7);
            }
        } else if (piece.getPieceType() == PieceType.QUEEN) {
            int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            for (int i = 0; i < directions.length; i++) {
                walk(row, col, directions[i][0], directions[i][1], possibleMoves, board, myPosition, 7);
            }
        } else if (piece.getPieceType() == PieceType.KING) {
            int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            for (int i = 0; i < directions.length; i++) {
                walk(row, col, directions[i][0], directions[i][1], possibleMoves, board, myPosition, 1);
            }
        } else if (piece.getPieceType() == PieceType.KNIGHT) {
            int[][] directions = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
            for (int i = 0; i < directions.length; i++) {
                walk(row, col, directions[i][0], directions[i][1], possibleMoves, board, myPosition, 1);
            }
        }

        return possibleMoves;
    }

    private void walk(int startRow, int startCol, int changeInRow, int changeInCol, List<ChessMove> possibleMoves,
                      ChessBoard board, ChessPosition myPosition, int maxSteps) {
        int checkingRow = startRow + changeInRow;
        int checkingCol = startCol + changeInCol;
        int steps = 0;
        while (checkingRow > 0 && checkingRow < 9 && checkingCol > 0 && checkingCol < 9 && steps < maxSteps) {
            ChessPosition checkingPosition = new ChessPosition(checkingRow, checkingCol);
            ChessPiece pieceInSpot = board.getPiece(checkingPosition);
            if (pieceInSpot == null || pieceInSpot.getTeamColor() != color) {
                possibleMoves.add(new ChessMove(myPosition, checkingPosition, null));
            }
            if (pieceInSpot != null) {
                    break;
            }
            checkingRow += changeInRow;
            checkingCol += changeInCol;
            steps += 1;
        }
    }
}
