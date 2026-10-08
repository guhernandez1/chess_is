package com.tpo.chess.core.models;

/**
 * Tipo de pieza. Es solo un identificador semántico: el comportamiento de movimiento
 * NO vive aquí sino en las {@link com.tpo.chess.core.movement.MovementStrategy} con
 * las que se compone cada {@link Piece}.
 */
public enum PieceType {
    PAWN,
    ROOK,
    KNIGHT,
    BISHOP,
    QUEEN,
    KING
}
