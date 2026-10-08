package com.tpo.chess.core.board;

import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.PieceType;
import com.tpo.chess.core.models.Position;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.tpo.chess.core.TestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void positionIsAValueObjectUsableAsMapKey() {
        Map<Position, String> map = new HashMap<>();
        map.put(new Position(3, 4), "e4");
        assertEquals("e4", map.get(new Position(3, 4)));
        assertTrue(new Position(0, 0).isOnBoard());
        assertFalse(new Position(8, 0).isOnBoard());
        assertFalse(new Position(0, -1).offset(0, 0).isOnBoard());
        assertEquals(new Position(4, 5), new Position(3, 4).offset(1, 1));
    }

    @Test
    void placeGetAndRemovePieces() {
        Board board = new Board();
        Piece rook = wR();
        board.placePiece(sq("a1"), rook);

        assertSame(rook, board.getPiece(sq("a1")));
        assertFalse(board.isEmpty(sq("a1")));
        assertTrue(board.isEmpty(sq("a2")));
        assertSame(rook, board.removePiece(sq("a1")));
        assertTrue(board.isEmpty(sq("a1")));
        assertNull(board.removePiece(sq("a1")));
    }

    @Test
    void movePieceReturnsCapturedPiece() {
        Board board = boardWith("a1", wR(), "a8", bR());
        Piece captured = board.movePiece(sq("a1"), sq("a8"));

        assertEquals(Color.BLACK, captured.getColor());
        assertEquals(Color.WHITE, board.getPiece(sq("a8")).getColor());
        assertTrue(board.isEmpty(sq("a1")));
    }

    @Test
    void movingFromEmptySquareFails() {
        assertThrows(IllegalStateException.class, () -> new Board().movePiece(sq("a1"), sq("a2")));
    }

    @Test
    void positionsOutsideTheBoardAreRejected() {
        Board board = new Board();
        assertThrows(IllegalArgumentException.class, () -> board.placePiece(new Position(8, 8), wK()));
        assertThrows(IllegalArgumentException.class, () -> board.getPiece(new Position(-1, 0)));
    }

    @Test
    void copyIsIndependent() {
        Board original = boardWith("e1", wK());
        Board copy = original.copy();
        copy.movePiece(sq("e1"), sq("e2"));

        assertFalse(original.isEmpty(sq("e1")));
        assertTrue(copy.isEmpty(sq("e1")));
    }

    @Test
    void findKingAndPiecesByColor() {
        Board board = BoardFactory.createStandardBoard();

        assertEquals(sq("e1"), board.findKing(Color.WHITE).orElseThrow());
        assertEquals(sq("e8"), board.findKing(Color.BLACK).orElseThrow());
        assertEquals(16, board.getPiecesOf(Color.WHITE).size());
        assertEquals(16, board.getPiecesOf(Color.BLACK).size());
        assertEquals(32, board.getAllPieces().size());
        assertEquals(PieceType.QUEEN, board.getPiece(sq("d1")).getPieceType());
        assertEquals(PieceType.QUEEN, board.getPiece(sq("d8")).getPieceType());
    }
}
