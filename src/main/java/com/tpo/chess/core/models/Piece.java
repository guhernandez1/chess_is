package com.tpo.chess.core.models;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.movement.MovementStrategy;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Pieza de ajedrez. Es una única clase concreta (no hay {@code Pawn extends Piece}, etc.):
 * su comportamiento se obtiene por <b>composición</b> de una o más {@link MovementStrategy}.
 *
 * <p>Ejemplo: la Reina = {@code LinearMovement} + {@code DiagonalMovement}.
 * Una pieza "fantasía" que combine caballo y torre se crea sin ninguna clase nueva.</p>
 *
 * <p>Es inmutable, por lo que puede compartirse entre copias del tablero sin riesgo.</p>
 */
public final class Piece {

    private final Color color;
    private final PieceType pieceType;
    private final List<MovementStrategy> strategies;

    public Piece(Color color, PieceType pieceType, List<MovementStrategy> strategies) {
        this.color = Objects.requireNonNull(color, "color");
        this.pieceType = Objects.requireNonNull(pieceType, "pieceType");
        Objects.requireNonNull(strategies, "strategies");
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("Una pieza necesita al menos una estrategia de movimiento");
        }
        this.strategies = List.copyOf(strategies);
    }

    /**
     * Movimientos pseudo-legales: respetan la forma de mover de la pieza, los bloqueos
     * y las capturas, pero NO verifican si dejan al propio rey en jaque
     * (eso es responsabilidad de {@link com.tpo.chess.core.rules.RuleValidator}).
     */
    public List<Position> getPseudoLegalMoves(Board board, Position start) {
        Set<Position> moves = new LinkedHashSet<>();
        for (MovementStrategy strategy : strategies) {
            moves.addAll(strategy.getValidMoves(board, start, this));
        }
        return List.copyOf(moves);
    }

    public Color getColor() {
        return color;
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public List<MovementStrategy> getStrategies() {
        return strategies;
    }

    public boolean isEnemyOf(Piece other) {
        return other != null && other.color != this.color;
    }

    @Override
    public String toString() {
        return color + " " + pieceType;
    }
}
