package chess;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard gameBoard;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.gameBoard = new ChessBoard();
        gameBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece pieceInSpot = gameBoard.getPiece(startPosition);
        if (pieceInSpot == null){
            return null;
        }
        Collection<ChessMove> possibleMoves = pieceInSpot.pieceMoves(gameBoard, startPosition);
        Collection<ChessMove> validMoveList = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            if (!validateMove(move)) {
                validMoveList.add(move);
            }
        }

        return validMoveList;
    }

    private boolean validateMove(ChessMove move) {
        // Get all the info I need
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType oldType = gameBoard.getPiece(startPosition).getPieceType();
        ChessGame.TeamColor myTeamColor = gameBoard.getPiece(startPosition).getTeamColor();
        ChessPiece pieceCaptured = gameBoard.getPiece(endPosition);

        // Make the move
        makeTestMove(move);
        // See if I'm in check
        boolean inCheck = isInCheck(myTeamColor);
        // Undo the move
        ChessMove undoMove = new ChessMove(endPosition, startPosition, oldType);
        makeTestMove(undoMove);
        gameBoard.addPiece(endPosition, pieceCaptured);

        return inCheck;
    }

    private void makeTestMove(ChessMove move){
        // ONLY CALL WHEN I'M SURE THERE IS A PIECE THERE.
        // THAT'S WHY IT'S PRIVATE
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        ChessPiece pieceInSpot = gameBoard.getPiece(startPosition);

        ChessPiece.PieceType newPieceType = move.getPromotionPiece();
        ChessGame.TeamColor teamColor = pieceInSpot.getTeamColor();
        if (newPieceType == null) {
            newPieceType = pieceInSpot.getPieceType();
        }
        gameBoard.addPiece(endPosition, new ChessPiece(teamColor, newPieceType));
        gameBoard.addPiece(startPosition, null);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        ChessPiece pieceInSpot = gameBoard.getPiece(startPosition);
        if (pieceInSpot == null) {
            throw new InvalidMoveException("No piece there");
        }

        Collection<ChessMove> validMoves = validMoves(startPosition);
        TeamColor pieceColor = pieceInSpot.getTeamColor();
        if (!validMoves.contains(move) || pieceColor != teamTurn) {
            throw new InvalidMoveException("You can't do that!");
        }

        ChessPiece.PieceType newPieceType = move.getPromotionPiece();
        ChessGame.TeamColor teamColor = pieceInSpot.getTeamColor();
        if (newPieceType == null) {
            newPieceType = pieceInSpot.getPieceType();
        }
        gameBoard.addPiece(endPosition, new ChessPiece(teamColor, newPieceType));
        gameBoard.addPiece(startPosition, null);
        if (teamTurn == TeamColor.WHITE){
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) { 
        ChessPosition myKingPosition = null;
        Collection<ChessMove> enemyMoves = new ArrayList<>();
        for (int checkingRow = 8; checkingRow > 0; checkingRow--) {
            for (int checkingCol = 1; checkingCol < 9; checkingCol++) {
                ChessPosition checkingPosition = new ChessPosition(checkingRow, checkingCol);
                ChessPiece pieceInSpot = gameBoard.getPiece(checkingPosition);
                if (pieceInSpot == null) {
                    assert true;
                } else if (pieceInSpot.getTeamColor() != teamColor) {
                    Collection<ChessMove> pieceInSpotMoves = pieceInSpot.pieceMoves(gameBoard, checkingPosition);
                    enemyMoves.addAll(pieceInSpotMoves);
                } else if (pieceInSpot.getTeamColor() == teamColor
                        && pieceInSpot.getPieceType() == ChessPiece.PieceType.KING) {
                    myKingPosition = checkingPosition;
                }
            }
        }

        for (ChessMove move : enemyMoves) {
            if (move.getEndPosition().equals(myKingPosition)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return isInStalemate(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Collection<ChessMove> myMoves = new ArrayList<>();
        for (int checkingRow = 8; checkingRow > 0; checkingRow--) {
            for (int checkingCol = 1; checkingCol < 9; checkingCol++) {
                ChessPosition checkingPosition = new ChessPosition(checkingRow, checkingCol);
                ChessPiece pieceInSpot = gameBoard.getPiece(checkingPosition);
                if (pieceInSpot == null) {
                    assert true;
                } else if (pieceInSpot.getTeamColor() == teamColor) {
                    Collection<ChessMove> pieceInSpotMoves = validMoves(checkingPosition);
                    myMoves.addAll(pieceInSpotMoves);
                }
            }
        }

        if (myMoves.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(gameBoard, chessGame.gameBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, gameBoard);
    }
}
