package com.tpo.chess.core.movement;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Movimiento del peón: avance de 1 (o 2 desde su fila inicial) a casillas vacías
 * y captura en diagonal hacia adelante.
 *
 * <p>La dirección se deduce del color de la pieza, por lo que una sola instancia sirve
 * para ambos bandos. Captura al paso queda fuera del alcance del MVP.</p>
 */
public final class PawnMovement implements MovementStrategy {

    @Override
    public List<Position> getValidMoves(Board board, Position start, Piece piece) {
        List<Position> moves = new ArrayList<>();
        int dir = direction(piece.getColor());

        Position oneStep = start.offset(dir, 0);
        if (oneStep.isOnBoard() && board.isEmpty(oneStep)) {
            moves.add(oneStep);
            Position twoSteps = start.offset(2 * dir, 0);
            if (start.row() == startingRow(piece.getColor()) && twoSteps.isOnBoard() && board.isEmpty(twoSteps)) {
                moves.add(twoSteps);
            }
        }

        for (int side : new int[]{-1, 1}) {
            Position diagonal = start.offset(dir, side);
            if (diagonal.isOnBoard() && piece.isEnemyOf(board.getPiece(diagonal))) {
                moves.add(diagonal);
            }
        }
        return moves;
    }

    /** +1 para blancas (suben de fila), -1 para negras. */
    public static int direction(Color color) {
        return color == Color.WHITE ? 1 : -1;
    }

    public static int startingRow(Color color) {
        return color == Color.WHITE ? 1 : Position.BOARD_SIZE - 2;
    }

    /** Fila en la que un peón de este color corona. */
    public static int promotionRow(Color color) {
        return color == Color.WHITE ? Position.BOARD_SIZE - 1 : 0;
    }
}
