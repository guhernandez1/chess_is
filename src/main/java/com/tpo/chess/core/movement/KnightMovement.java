package com.tpo.chess.core.movement;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Salto en "L" del caballo. Ignora piezas intermedias.
 */
public final class KnightMovement implements MovementStrategy {

    private static final int[][] JUMPS = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };

    @Override
    public List<Position> getValidMoves(Board board, Position start, Piece piece) {
        List<Position> moves = new ArrayList<>();
        for (int[] jump : JUMPS) {
            Position target = start.offset(jump[0], jump[1]);
            if (!target.isOnBoard()) {
                continue;
            }
            Piece occupant = board.getPiece(target);
            if (occupant == null || piece.isEnemyOf(occupant)) {
                moves.add(target);
            }
        }
        return moves;
    }
}
