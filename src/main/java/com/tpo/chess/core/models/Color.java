package com.tpo.chess.core.models;

/**
 * Bando de una pieza / jugador.
 */
public enum Color {
    WHITE,
    BLACK;

    /** @return el color contrario. */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
