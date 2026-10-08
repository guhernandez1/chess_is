package com.tpo.chess.core.board;

import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.PieceType;
import com.tpo.chess.core.models.Position;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Tablero de 8x8. Solo gestiona la ubicación de las piezas (SRP): no conoce reglas de
 * turnos ni de jaque.
 *
 * <p>Las casillas vacías simplemente no están en el mapa.</p>
 */
public final class Board {

    private final Map<Position, Piece> grid;

    public Board() {
        this.grid = new HashMap<>();
    }

    private Board(Map<Position, Piece> grid) {
        this.grid = new HashMap<>(grid);
    }

    public void placePiece(Position pos, Piece piece) {
        requireOnBoard(pos);
        grid.put(pos, Objects.requireNonNull(piece, "piece"));
    }

    /** @return la pieza removida, o {@code null} si la casilla estaba vacía. */
    public Piece removePiece(Position pos) {
        requireOnBoard(pos);
        return grid.remove(pos);
    }

    /** @return la pieza en la casilla, o {@code null} si está vacía. */
    public Piece getPiece(Position pos) {
        requireOnBoard(pos);
        return grid.get(pos);
    }

    public boolean isEmpty(Position pos) {
        return getPiece(pos) == null;
    }

    /**
     * Mueve la pieza de {@code from} a {@code to} sin validar reglas.
     *
     * @return la pieza capturada en {@code to}, o {@code null}.
     * @throws IllegalStateException si no hay pieza en {@code from}.
     */
    public Piece movePiece(Position from, Position to) {
        requireOnBoard(to);
        Piece moving = removePiece(from);
        if (moving == null) {
            throw new IllegalStateException("No hay pieza en " + from);
        }
        Piece captured = grid.put(to, moving);
        return captured;
    }

    public Optional<Position> findKing(Color color) {
        return grid.entrySet().stream()
                .filter(e -> e.getValue().getColor() == color && e.getValue().getPieceType() == PieceType.KING)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /** @return vista inmutable de las piezas de un color con su posición. */
    public Map<Position, Piece> getPiecesOf(Color color) {
        Map<Position, Piece> result = new LinkedHashMap<>();
        grid.forEach((pos, piece) -> {
            if (piece.getColor() == color) {
                result.put(pos, piece);
            }
        });
        return Collections.unmodifiableMap(result);
    }

    /** @return vista inmutable de todas las piezas del tablero. */
    public Map<Position, Piece> getAllPieces() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(grid));
    }

    /**
     * Copia independiente del tablero. Como {@link Piece} y {@link Position} son inmutables,
     * una copia superficial del mapa es suficiente y barata (se usa para simular jugadas).
     */
    public Board copy() {
        return new Board(grid);
    }

    private static void requireOnBoard(Position pos) {
        Objects.requireNonNull(pos, "position");
        if (!pos.isOnBoard()) {
            throw new IllegalArgumentException("Posición fuera del tablero: " + pos);
        }
    }
}
