package com.tpo.chess.core;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.board.PieceFactory;
import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.Position;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilidades de test en Java puro: permiten escribir casillas en notación algebraica
 * ("e4") para que los tests sean legibles.
 */
public final class TestSupport {

    private TestSupport() {
    }

    public static Position sq(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    public static Set<Position> squares(String... squares) {
        return Arrays.stream(squares).map(TestSupport::sq).collect(Collectors.toSet());
    }

    public static Set<Position> asSet(List<Position> positions) {
        return Set.copyOf(positions);
    }

    public static Board boardWith(Object... squareAndPiece) {
        Board board = new Board();
        for (int i = 0; i < squareAndPiece.length; i += 2) {
            board.placePiece(sq((String) squareAndPiece[i]), (Piece) squareAndPiece[i + 1]);
        }
        return board;
    }

    public static Piece wK() { return PieceFactory.king(Color.WHITE); }
    public static Piece bK() { return PieceFactory.king(Color.BLACK); }
    public static Piece wQ() { return PieceFactory.queen(Color.WHITE); }
    public static Piece bQ() { return PieceFactory.queen(Color.BLACK); }
    public static Piece wR() { return PieceFactory.rook(Color.WHITE); }
    public static Piece bR() { return PieceFactory.rook(Color.BLACK); }
    public static Piece wB() { return PieceFactory.bishop(Color.WHITE); }
    public static Piece bB() { return PieceFactory.bishop(Color.BLACK); }
    public static Piece wN() { return PieceFactory.knight(Color.WHITE); }
    public static Piece bN() { return PieceFactory.knight(Color.BLACK); }
    public static Piece wP() { return PieceFactory.pawn(Color.WHITE); }
    public static Piece bP() { return PieceFactory.pawn(Color.BLACK); }
}
