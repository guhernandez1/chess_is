package com.tpo.chess.core.movement;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.board.BoardFactory;
import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.PieceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tpo.chess.core.TestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class MovementStrategyTest {

    @Test
    void rookSlidesOrthogonallyOnEmptyBoard() {
        Board board = boardWith("d4", wR());
        assertEquals(14, board.getPiece(sq("d4")).getPseudoLegalMoves(board, sq("d4")).size());
    }

    @Test
    void rookIsBlockedByOwnPiecesAndCapturesEnemyButStops() {
        Board board = boardWith("a1", wR(), "a3", wP(), "c1", bN());
        var moves = asSet(board.getPiece(sq("a1")).getPseudoLegalMoves(board, sq("a1")));
        assertEquals(squares("a2", "b1", "c1"), moves);
    }

    @Test
    void bishopSlidesDiagonally() {
        Board board = boardWith("d4", wB());
        var moves = board.getPiece(sq("d4")).getPseudoLegalMoves(board, sq("d4"));
        assertEquals(13, moves.size());
        assertTrue(moves.contains(sq("a1")));
        assertTrue(moves.contains(sq("h8")));
        assertFalse(moves.contains(sq("d5")));
    }

    @Test
    void queenCombinesLinearAndDiagonal() {
        Board board = boardWith("d4", wQ());
        assertEquals(27, board.getPiece(sq("d4")).getPseudoLegalMoves(board, sq("d4")).size());
    }

    @Test
    void kingMovesOneSquareInAnyDirection() {
        Board board = boardWith("d4", wK(), "a1", bK());
        assertEquals(8, board.getPiece(sq("d4")).getPseudoLegalMoves(board, sq("d4")).size());
        assertEquals(squares("a2", "b1", "b2"),
                asSet(board.getPiece(sq("a1")).getPseudoLegalMoves(board, sq("a1"))));
    }

    @Test
    void knightJumpsOverPieces() {
        Board board = BoardFactory.createStandardBoard();
        assertEquals(squares("a3", "c3"), asSet(board.getPiece(sq("b1")).getPseudoLegalMoves(board, sq("b1"))));
    }

    @Test
    void knightCannotLandOnOwnPiece() {
        Board board = boardWith("d4", wN(), "e6", wP(), "c6", bP());
        var moves = board.getPiece(sq("d4")).getPseudoLegalMoves(board, sq("d4"));
        assertEquals(7, moves.size());
        assertFalse(moves.contains(sq("e6")));
        assertTrue(moves.contains(sq("c6")));
    }

    @Test
    void pawnAdvancesOneOrTwoFromStartingRow() {
        Board board = boardWith("e2", wP(), "d7", bP());
        assertEquals(squares("e3", "e4"), asSet(board.getPiece(sq("e2")).getPseudoLegalMoves(board, sq("e2"))));
        assertEquals(squares("d6", "d5"), asSet(board.getPiece(sq("d7")).getPseudoLegalMoves(board, sq("d7"))));
    }

    @Test
    void pawnAdvancesOnlyOneOutsideStartingRow() {
        Board board = boardWith("e3", wP());
        assertEquals(squares("e4"), asSet(board.getPiece(sq("e3")).getPseudoLegalMoves(board, sq("e3"))));
    }

    @Test
    void pawnIsBlockedByAnyPieceInFront() {
        Board blockedAdjacent = boardWith("e2", wP(), "e3", bP());
        assertTrue(blockedAdjacent.getPiece(sq("e2")).getPseudoLegalMoves(blockedAdjacent, sq("e2")).isEmpty());

        Board blockedTwoAhead = boardWith("e2", wP(), "e4", bP());
        assertEquals(squares("e3"),
                asSet(blockedTwoAhead.getPiece(sq("e2")).getPseudoLegalMoves(blockedTwoAhead, sq("e2"))));
    }

    @Test
    void pawnCapturesDiagonallyOnlyEnemies() {
        Board board = boardWith("e4", wP(), "d5", bP(), "f5", wN());
        assertEquals(squares("e5", "d5"), asSet(board.getPiece(sq("e4")).getPseudoLegalMoves(board, sq("e4"))));
    }

    /**
     * Demostración de composición sobre herencia: una pieza "fantasía" (Canciller =
     * Caballo + Torre) se crea sin escribir ninguna clase nueva.
     */
    @Test
    void newPieceTypesCanBeComposedWithoutNewClasses() {
        Piece chancellor = new Piece(Color.WHITE, PieceType.QUEEN,
                List.of(new KnightMovement(), new LinearMovement()));
        Board board = boardWith("d4", chancellor);
        assertEquals(8 + 14, chancellor.getPseudoLegalMoves(board, sq("d4")).size());
    }

    @Test
    void pieceRequiresAtLeastOneStrategy() {
        assertThrows(IllegalArgumentException.class, () -> new Piece(Color.WHITE, PieceType.PAWN, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new LinearMovement(0));
    }
}
