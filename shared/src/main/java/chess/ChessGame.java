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
    private Collection<ChessMove> possibleEnPassant;
    private boolean whiteCastleRight;
    private boolean whiteCastleLeft;
    private boolean blackCastleRight;
    private boolean blackCastleLeft;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.gameBoard = new ChessBoard();
        gameBoard.resetBoard();

        this.possibleEnPassant = new ArrayList<>();
        this.whiteCastleRight = true;
        this.whiteCastleLeft = true;
        this.blackCastleRight = true;
        this.blackCastleLeft = true;
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
            if (validateMove(move)) {
                validMoveList.add(move);
            }
        }

        if (!possibleEnPassant.isEmpty()) {
            for (ChessMove move : possibleEnPassant) {
                if (move.getStartPosition().equals(startPosition) && validateEnPassant(move)) {
                    validMoveList.add(move);
                }
            }
        }

        if (pieceInSpot.getPieceType() == ChessPiece.PieceType.KING) {
            validMoveList.addAll(validateCastle(startPosition, pieceInSpot.getTeamColor()));
        }

        return validMoveList;
    }

    private boolean validateMove(ChessMove move) {
        // Get all the info I need
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType oldType = gameBoard.getPiece(startPosition).getPieceType();
        TeamColor myTeamColor = gameBoard.getPiece(startPosition).getTeamColor();
        ChessPiece pieceCaptured = gameBoard.getPiece(endPosition);

        // Make the move
        makeTestMove(move);
        // See if I'm in check
        boolean inCheck = isInCheck(myTeamColor);
        // Undo the move
        ChessMove undoMove = new ChessMove(endPosition, startPosition, oldType);
        makeTestMove(undoMove);
        gameBoard.addPiece(endPosition, pieceCaptured);

        return !inCheck;
    }

    private void makeTestMove(ChessMove move){
        // ONLY CALL WHEN I'M SURE THERE IS A PIECE THERE.
        // THAT'S WHY IT'S PRIVATE
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        ChessPiece pieceInSpot = gameBoard.getPiece(startPosition);

        ChessPiece.PieceType newPieceType = move.getPromotionPiece();
        TeamColor teamColor = pieceInSpot.getTeamColor();
        if (newPieceType == null) {
            newPieceType = pieceInSpot.getPieceType();
        }
        gameBoard.addPiece(endPosition, new ChessPiece(teamColor, newPieceType));
        gameBoard.addPiece(startPosition, null);
    }

    private boolean validateEnPassant(ChessMove move) {
        // Get all the info I need
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        TeamColor myTeamColor = gameBoard.getPiece(startPosition).getTeamColor();
        ChessPosition pieceCapturedPosition = new ChessPosition(startPosition.getRow(), endPosition.getColumn());
        ChessPiece pieceCaptured = gameBoard.getPiece(pieceCapturedPosition);

        // Make the move
        makeTestMove(move);
        gameBoard.addPiece(pieceCapturedPosition, null);
        // See if I'm in check
        boolean inCheck = isInCheck(myTeamColor);
        // Undo the move
        ChessMove undoMove = new ChessMove(endPosition, startPosition, null);
        makeTestMove(undoMove);
        gameBoard.addPiece(pieceCapturedPosition, pieceCaptured);

        return !inCheck;
    }

    private Collection<ChessMove> validateCastle(ChessPosition startPosition, TeamColor kingColor) {
        Collection<ChessMove> possibleCastles = new ArrayList<>();
        ChessPosition firstSpot;
        ChessPosition secondSpot;
        ChessPosition thirdSpot;
        ChessPosition rookSpot;
        ChessPiece rookPiece;
        if (((whiteCastleRight && kingColor == TeamColor.WHITE) || (blackCastleRight && kingColor == TeamColor.BLACK))
                && startPosition.getColumn() == 5) {
            firstSpot = new ChessPosition(startPosition.getRow(), startPosition.getColumn()+1);
            secondSpot = new ChessPosition(startPosition.getRow(), startPosition.getColumn()+2);
            rookSpot = new ChessPosition(startPosition.getRow(), 8);
            rookPiece = gameBoard.getPiece(rookSpot);
            if (gameBoard.getPiece(firstSpot) != null || gameBoard.getPiece(secondSpot) != null
                    || isInCheck(kingColor) || rookPiece == null
                    || rookPiece.getPieceType() != ChessPiece.PieceType.ROOK
                    || startPosition.getColumn() != 5) {
                assert true;
            } else {
                checkIfCastleIntoCheck(startPosition, firstSpot, secondSpot, possibleCastles);
            }
        }
        if (((whiteCastleLeft && kingColor == TeamColor.WHITE) || (blackCastleLeft && kingColor == TeamColor.BLACK))
                && startPosition.getColumn() == 5) {
            firstSpot = new ChessPosition(startPosition.getRow(), startPosition.getColumn()-1);
            secondSpot = new ChessPosition(startPosition.getRow(), startPosition.getColumn()-2);
            thirdSpot = new ChessPosition(startPosition.getRow(), startPosition.getColumn()-3);
            rookSpot = new ChessPosition(startPosition.getRow(), 1);
            rookPiece = gameBoard.getPiece(rookSpot);
            if (gameBoard.getPiece(firstSpot) != null || gameBoard.getPiece(secondSpot) != null
                    || gameBoard.getPiece(thirdSpot) != null || isInCheck(kingColor) || rookPiece == null
                    || rookPiece.getPieceType() != ChessPiece.PieceType.ROOK) {
                assert true;
            } else {
                checkIfCastleIntoCheck(startPosition, firstSpot, secondSpot, possibleCastles);
            }
        }

        return possibleCastles;
    }

    private void checkIfCastleIntoCheck(ChessPosition startPosition, ChessPosition firstSpot,
                                        ChessPosition secondSpot, Collection<ChessMove> possibleCastles) {
        ChessMove firstMove = new ChessMove(startPosition, firstSpot, null);
        ChessMove secondMove = new ChessMove(firstSpot, secondSpot, null);
        if (validateCastleMove(firstMove, secondMove)) {
            ChessMove castle = new ChessMove(startPosition, secondSpot, null);
            possibleCastles.add(castle);
        }
    }

    private boolean validateCastleMove(ChessMove firstMove, ChessMove secondMove) {
        // Get all the info I need
        ChessPosition startPosition = firstMove.getStartPosition();
        ChessPosition firstPosition = firstMove.getEndPosition();
        ChessPosition endPosition = secondMove.getEndPosition();
        TeamColor myTeamColor = gameBoard.getPiece(startPosition).getTeamColor();
        ChessMove undoMove;

        // Make the move
        makeTestMove(firstMove);
        // See if I'm in check
        boolean inCheck = isInCheck(myTeamColor);
        if (!inCheck) {
            makeTestMove(secondMove);
            inCheck = isInCheck(myTeamColor);
            undoMove = new ChessMove(endPosition, startPosition, null);
        } else {
            undoMove = new ChessMove(firstPosition, startPosition, null);
        }
        // Undo the move
        makeTestMove(undoMove);

        return !inCheck;
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

        if (pieceInSpot.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (startPosition.getRow() - endPosition.getRow() == -2 ||
                    startPosition.getRow() - endPosition.getRow() == 2) {
                pawnMovedEnPassantCheck(move, pieceInSpot);
            }
        }

        checkIfMoveBreaksCastle(move);

        ChessPiece.PieceType newPieceType = move.getPromotionPiece();
        if (newPieceType == null) {
            newPieceType = pieceInSpot.getPieceType();
        }
        gameBoard.addPiece(endPosition, new ChessPiece(pieceColor, newPieceType));
        gameBoard.addPiece(startPosition, null);
        if (possibleEnPassant.contains(move)) {
            gameBoard.addPiece(new ChessPosition(startPosition.getRow(), endPosition.getColumn()), null);
            possibleEnPassant.remove(move);
        }
        if (!possibleEnPassant.isEmpty()) {
            List<ChessMove> toRemove = new ArrayList<>();
            for (ChessMove enPassant : possibleEnPassant) {
                ChessPiece pieceInStartSpot = gameBoard.getPiece(enPassant.getStartPosition());
                TeamColor teamColor = pieceInStartSpot.getTeamColor();
                if (teamColor == teamTurn) {
                   toRemove.add(enPassant);
                }
            }
            possibleEnPassant.removeAll(toRemove);
        }

        // Check if it was a castle and moves the rook if it needs to
        if (gameBoard.getPiece(endPosition).getPieceType() == ChessPiece.PieceType.KING) {
            int distance = endPosition.getColumn() - startPosition.getColumn();
            if (distance == 2) {
                gameBoard.addPiece(new ChessPosition(endPosition.getRow(), 8), null);
                gameBoard.addPiece(new ChessPosition(endPosition.getRow(), 6),
                        new ChessPiece(teamTurn, ChessPiece.PieceType.ROOK));
            } else if (distance == -2) {
                gameBoard.addPiece(new ChessPosition(endPosition.getRow(), 1), null);
                gameBoard.addPiece(new ChessPosition(endPosition.getRow(), 4),
                        new ChessPiece(teamTurn, ChessPiece.PieceType.ROOK));
            }
        }

        if (teamTurn == TeamColor.WHITE){
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }

    }

    private void pawnMovedEnPassantCheck(ChessMove move, ChessPiece pieceInSpot) {
        ChessPosition endPosition = move.getEndPosition();
        if (endPosition.getColumn() > 1) {
            ChessPosition leftPosition = new ChessPosition(endPosition.getRow(), endPosition.getColumn()-1);
            ChessPiece pieceInLeft = gameBoard.getPiece(leftPosition);
            if (pieceInLeft != null && pieceInLeft.getTeamColor() != pieceInSpot.getTeamColor()
                    && pieceInLeft.getPieceType() == ChessPiece.PieceType.PAWN) {
                ChessPosition enPassantEndPosition;
                if (pieceInLeft.getTeamColor() == TeamColor.WHITE) {
                    enPassantEndPosition = new ChessPosition(endPosition.getRow()+1, endPosition.getColumn());
                } else {
                    enPassantEndPosition = new ChessPosition(endPosition.getRow()-1, endPosition.getColumn());
                }
                ChessMove enPassantMove = new ChessMove(leftPosition, enPassantEndPosition, null);
                possibleEnPassant.add(enPassantMove);
            }
        }
        if (endPosition.getColumn() < 8) {
            ChessPosition rightPosition = new ChessPosition(endPosition.getRow(),
                    endPosition.getColumn()+1);
            ChessPiece pieceInRight = gameBoard.getPiece(rightPosition);
            if (pieceInRight != null && pieceInRight.getTeamColor() != pieceInSpot.getTeamColor()
                    && pieceInRight.getPieceType() == ChessPiece.PieceType.PAWN) {
                ChessPosition enPassantEndPosition;
                if (pieceInRight.getTeamColor() == TeamColor.WHITE) {
                    enPassantEndPosition = new ChessPosition(endPosition.getRow()+1,
                            endPosition.getColumn());
                } else {
                    enPassantEndPosition = new ChessPosition(endPosition.getRow()-1,
                            endPosition.getColumn());
                }
                ChessMove enPassantMove = new ChessMove(rightPosition, enPassantEndPosition, null);
                possibleEnPassant.add(enPassantMove);
            }
        }
    }

    /**
     * Checks if a move that was just made (or about to be made)
     * makes it so that the team can no longer castle,
     * or in other words it checks if the king is moving or
     * one of the rooks. Fun stuff. I should probably write these
     * for more of my private helper methods, at least to stay organized
     *
     * @param move the move being performed
     */
    private void checkIfMoveBreaksCastle(ChessMove move) {
        ChessPiece pieceMoving = gameBoard.getPiece(move.getStartPosition());
        TeamColor pieceMovingColor = pieceMoving.getTeamColor();
        if (pieceMoving.getPieceType() == ChessPiece.PieceType.KING) {
            if (pieceMovingColor == TeamColor.WHITE) {
                whiteCastleLeft = false;
                whiteCastleRight = false;
            } else {
                blackCastleLeft = false;
                blackCastleRight = false;
            }
        } else if (pieceMoving.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (pieceMovingColor == TeamColor.WHITE) {
                ChessPosition rightStartPosition = new ChessPosition(1, 8);
                ChessPosition leftStartPosition = new ChessPosition(1, 1);
                if (whiteCastleRight && move.getStartPosition().equals(rightStartPosition)) {
                    whiteCastleRight = false;
                } else if (whiteCastleLeft && move.getStartPosition().equals(leftStartPosition)) {
                    whiteCastleLeft = false;
                }
            } else {
                ChessPosition rightStartPosition = new ChessPosition(8, 8);
                ChessPosition leftStartPosition = new ChessPosition(8, 1);
                if (blackCastleRight && move.getStartPosition().equals(rightStartPosition)) {
                    blackCastleRight = false;
                } else if (blackCastleLeft && move.getStartPosition().equals(leftStartPosition)) {
                    blackCastleLeft = false;
                }
            }
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

        Collection<ChessMove> myMoves = new ArrayList<>(findValidTeamMoves(teamColor));

        if (myMoves.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        Collection<ChessMove> myMoves = new ArrayList<>(findValidTeamMoves(teamColor));

        if (myMoves.isEmpty()) {
            return true;
        }
        return false;
    }

    private Collection<ChessMove> findValidTeamMoves(TeamColor teamColor) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        for (int checkingRow = 8; checkingRow > 0; checkingRow--) {
            for (int checkingCol = 1; checkingCol < 9; checkingCol++) {
                ChessPosition checkingPosition = new ChessPosition(checkingRow, checkingCol);
                ChessPiece pieceInSpot = gameBoard.getPiece(checkingPosition);
                if (pieceInSpot == null) {
                    assert true;
                } else if (pieceInSpot.getTeamColor() == teamColor) {
                    Collection<ChessMove> pieceInSpotMoves = validMoves(checkingPosition);
                    possibleMoves.addAll(pieceInSpotMoves);
                }
            }
        }
        return possibleMoves;
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
