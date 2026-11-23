package com.tpo.chess.core.models;

/**
 * Convención: {@code row} 0 corresponde a la fila 1 y {@code col} 0 a la columna a.
 * Se admiten posiciones fuera del tablero para calcular desplazamientos; validar con {@link #isOnBoard()}.
 */
public record Position(int row, int col) {

    public static final int BOARD_SIZE = 8;

    public boolean isOnBoard() {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /** @return una nueva posición desplazada (puede quedar fuera del tablero). */
    public Position offset(int deltaRow, int deltaCol) {
        return new Position(row + deltaRow, col + deltaCol);
    }
}
