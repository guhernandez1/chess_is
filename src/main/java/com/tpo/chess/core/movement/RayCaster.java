package com.tpo.chess.core.movement;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad compartida para movimientos "deslizantes" (torre, alfil, reina, rey).
 * Se reutiliza por <b>delegación</b> en lugar de una clase base abstracta, para no
 * introducir herencia entre estrategias.
 */
final class RayCaster {

    private RayCaster() {
    }

    /**
     * Recorre cada dirección hasta {@code maxSteps} casillas, deteniéndose al salir del
     * tablero o al encontrar una pieza (incluida si es enemiga, excluida si es propia).
     */
    static List<Position> cast(Board board, Position start, Piece piece, int[][] directions, int maxSteps) {
        List<Position> moves = new ArrayList<>();
        for (int[] dir : directions) {
            Position current = start;
            for (int step = 0; step < maxSteps; step++) {
                current = current.offset(dir[0], dir[1]);
                if (!current.isOnBoard()) {
                    break;
                }
                Piece occupant = board.getPiece(current);
                if (occupant == null) {
                    moves.add(current);
                    continue;
                }
                if (piece.isEnemyOf(occupant)) {
                    moves.add(current);
                }
                break; // bloqueado
            }
        }
        return moves;
    }
}
