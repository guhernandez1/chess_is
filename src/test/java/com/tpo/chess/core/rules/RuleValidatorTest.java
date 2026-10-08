package com.tpo.chess.core.rules;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.board.BoardFactory;
import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.GameStatus;
import org.junit.jupiter.api.Test;

import static com.tpo.chess.core.TestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class RuleValidatorTest {

    private final RuleValidator validator = new RuleValidator();

    @Test
    void detectsCheckFromSlidingPiece() {
        Board board = boardWith("e1", wK(), "e8", bR(), "a8", bK());
        assertTrue(validator.isInCheck(board, Color.WHITE));
        assertFalse(validator.isInCheck(board, Color.BLACK));
    }

    @Test
    void blockedAttackIsNotCheck() {
        Board board = boardWith("e1", wK(), "e2", wB(), "e8", bR(), "a8", bK());
        assertFalse(validator.isInCheck(board, Color.WHITE));
    }

    @Test
    void detectsCheckFromKnightAndPawn() {
        assertTrue(validator.isInCheck(boardWith("e1", wK(), "f3", bN(), "a8", bK()), Color.WHITE));
        assertTrue(validator.isInCheck(boardWith("e1", wK(), "d2", bP(), "a8", bK()), Color.WHITE));
        // Un peón NO da jaque hacia adelante.
        assertFalse(validator.isInCheck(boardWith("e1", wK(), "e2", bP(), "a8", bK()), Color.WHITE));
    }

    @Test
    void pinnedPieceCannotMove() {
        Board board = boardWith("e1", wK(), "e2", wB(), "e8", bR(), "a8", bK());
        assertTrue(validator.getLegalMoves(board, sq("e2")).isEmpty());
    }

    @Test
    void pinnedPieceMayMoveAlongThePinLine() {
        Board board = boardWith("e1", wK(), "e3", wR(), "e8", bR(), "a8", bK());
        assertEquals(squares("e2", "e4", "e5", "e6", "e7", "e8"), asSet(validator.getLegalMoves(board, sq("e3"))));
    }

    @Test
    void kingCannotMoveIntoAttackedSquare() {
        Board board = boardWith("e1", wK(), "d8", bR(), "a8", bK());
        var moves = asSet(validator.getLegalMoves(board, sq("e1")));
        assertFalse(moves.contains(sq("d1")));
        assertFalse(moves.contains(sq("d2")));
        assertEquals(squares("e2", "f1", "f2"), moves);
    }

    @Test
    void whenInCheckOnlyMovesThatResolveItAreLegal() {
        Board board = boardWith("e1", wK(), "e8", bR(), "a2", wR(), "a8", bK());
        assertEquals(squares("e2"), asSet(validator.getLegalMoves(board, sq("a2"))));
    }

    @Test
    void emptySquareHasNoLegalMoves() {
        assertTrue(validator.getLegalMoves(new Board(), sq("e4")).isEmpty());
    }

    @Test
    void initialPositionIsInProgressWithTwentyMoves() {
        Board board = BoardFactory.createStandardBoard();
        assertEquals(GameStatus.IN_PROGRESS, validator.evaluateStatus(board, Color.WHITE));
        int total = board.getPiecesOf(Color.WHITE).keySet().stream()
                .mapToInt(pos -> validator.getLegalMoves(board, pos).size()).sum();
        assertEquals(20, total);
    }

    @Test
    void evaluatesCheck() {
        Board board = boardWith("e1", wK(), "e8", bR(), "a8", bK());
        assertEquals(GameStatus.CHECK, validator.evaluateStatus(board, Color.WHITE));
    }

    @Test
    void evaluatesBackRankCheckmate() {
        Board board = boardWith("g1", wK(), "f2", wP(), "g2", wP(), "h2", wP(), "a1", bR(), "g8", bK());
        assertEquals(GameStatus.CHECKMATE, validator.evaluateStatus(board, Color.WHITE));
    }

    @Test
    void checkmateIsAvoidedIfAttackerCanBeCaptured() {
        Board board = boardWith("g1", wK(), "f2", wP(), "g2", wP(), "h2", wP(), "a1", bR(), "g8", bK(),
                "c3", wB());
        assertEquals(GameStatus.CHECK, validator.evaluateStatus(board, Color.WHITE));
    }

    @Test
    void evaluatesStalemate() {
        Board board = boardWith("a8", bK(), "b6", wQ(), "h1", wK());
        assertEquals(GameStatus.STALEMATE, validator.evaluateStatus(board, Color.BLACK));
    }
}
