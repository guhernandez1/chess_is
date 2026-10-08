package com.tpo.chess.core.models;

/**
 * Registro inmutable de una jugada ya ejecutada.
 *
 * @param from      casilla de origen
 * @param to        casilla de destino
 * @param color     bando que movió
 * @param pieceType tipo de pieza que movió (antes de una eventual promoción)
 * @param captured  tipo de pieza capturada, o {@code null} si no hubo captura
 * @param promotion {@code true} si un peón coronó en esta jugada
 */
public record Move(Position from, Position to, Color color, PieceType pieceType,
                   PieceType captured, boolean promotion) {

    public boolean isCapture() {
        return captured != null;
    }
}
