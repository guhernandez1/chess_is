package com.tpo.chess.core.models;

/**
 * Estado de la partida, evaluado desde el punto de vista del jugador que tiene el turno.
 */
public enum GameStatus {
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE;

    public boolean isFinished() {
        return this == CHECKMATE || this == STALEMATE;
    }
}
