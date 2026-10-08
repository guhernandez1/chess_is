package com.tpo.chess.core.models;

/**
 * Estado de la partida, evaluado desde el punto de vista del jugador que tiene el turno.
 */
public enum GameStatus {
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE;

    /** @return {@code true} si la partida terminó. */
    public boolean isFinished() {
        return this == CHECKMATE || this == STALEMATE;
    }
}
