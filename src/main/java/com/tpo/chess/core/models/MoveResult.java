package com.tpo.chess.core.models;

/**
 * Motivo del rechazo de una jugada, sin recurrir a excepciones.
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
