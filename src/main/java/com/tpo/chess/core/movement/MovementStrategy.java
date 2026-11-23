package com.tpo.chess.core.movement;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.Position;

import java.util.List;

/**
 * Devuelve movimientos pseudo-legales dentro del tablero, respetando bloqueos y sin capturar piezas propias.
 * No evalúa si el rey queda en jaque.
 */
public interface MovementStrategy {

    List<Position> getValidMoves(Board board, Position start, Piece piece);
}
