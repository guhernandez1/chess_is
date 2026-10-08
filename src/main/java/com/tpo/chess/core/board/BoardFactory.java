package com.tpo.chess.core.board;

import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.PieceType;
import com.tpo.chess.core.models.Position;

/**
 * Crea tableros con la disposición inicial estándar del ajedrez.
 */
public final class BoardFactory {

    private static final PieceType[] BACK_RANK = {
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
    };

    private BoardFactory() {
    }

    public static Board createStandardBoard() {
        Board board = new Board();
        for (int col = 0; col < Position.BOARD_SIZE; col++) {
            board.placePiece(new Position(0, col), PieceFactory.create(BACK_RANK[col], Color.WHITE));
            board.placePiece(new Position(1, col), PieceFactory.pawn(Color.WHITE));
            board.placePiece(new Position(6, col), PieceFactory.pawn(Color.BLACK));
            board.placePiece(new Position(7, col), PieceFactory.create(BACK_RANK[col], Color.BLACK));
        }
        return board;
    }
}
