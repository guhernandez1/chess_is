package com.tpo.chess.core.movement;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.Position;

import java.util.List;

/**
 * Estrategia de movimiento (patrón Strategy). Cada implementación encapsula UNA forma
 * de moverse; las piezas se arman combinando estrategias.
 *
 * <p>Contrato: devuelve movimientos pseudo-legales (dentro del tablero, respetando
 * bloqueos y sin capturar piezas propias). No evalúa jaques.</p>
 */
public interface MovementStrategy {

    List<Position> getValidMoves(Board board, Position start, Piece piece);
}
