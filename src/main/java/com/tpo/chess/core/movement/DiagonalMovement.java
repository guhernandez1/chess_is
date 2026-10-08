package com.tpo.chess.core.movement;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.Position;

import java.util.List;

/**
 * Movimiento en diagonal. Alfil, Reina y —con {@code maxSteps = 1}— Rey.
 */
public final class DiagonalMovement implements MovementStrategy {

    private static final int[][] DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    private final int maxSteps;

    /** Movimiento ilimitado (hasta el borde o un bloqueo). */
    public DiagonalMovement() {
        this(Position.BOARD_SIZE - 1);
    }

    public DiagonalMovement(int maxSteps) {
        if (maxSteps < 1) {
            throw new IllegalArgumentException("maxSteps debe ser >= 1");
        }
        this.maxSteps = maxSteps;
    }

    @Override
    public List<Position> getValidMoves(Board board, Position start, Piece piece) {
        return RayCaster.cast(board, start, piece, DIRECTIONS, maxSteps);
    }
}
