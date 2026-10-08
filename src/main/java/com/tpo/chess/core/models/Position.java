package com.tpo.chess.core.models;

/**
 * Casilla del tablero. Inmutable gracias a {@code record}: obtenemos
 * {@code equals/hashCode/toString} gratis, lo que la hace segura como clave de {@code Map}.
 *
 * <p>Convención: {@code row} 0 = fila "1" (lado blanco), {@code col} 0 = columna "a".</p>
 *
 * <p>Se permite construir posiciones fuera del tablero (útil al calcular desplazamientos);
 * usar {@link #isOnBoard()} para validarlas.</p>
 */
public record Position(int row, int col) {

    public static final int BOARD_SIZE = 8;

    /** @return {@code true} si la posición está dentro del tablero 8x8. */
    public boolean isOnBoard() {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /** @return una nueva posición desplazada (puede quedar fuera del tablero). */
    public Position offset(int deltaRow, int deltaCol) {
        return new Position(row + deltaRow, col + deltaCol);
    }
}
