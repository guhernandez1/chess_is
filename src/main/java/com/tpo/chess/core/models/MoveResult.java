package com.tpo.chess.core.models;

/**
 * Resultado detallado de intentar una jugada. Permite a los adaptadores explicar
 * por qué se rechazó un movimiento sin depender de excepciones.
 */
public enum MoveResult {
    SUCCESS,
    GAME_OVER,
    OUT_OF_BOARD,
    NO_PIECE,
    NOT_YOUR_TURN,
    ILLEGAL_MOVE;

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
