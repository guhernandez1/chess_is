package com.tpo.chess.core.rules;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.GameStatus;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.Position;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Valida la legalidad de las jugadas: filtra los movimientos pseudo-legales que dejarían
 * al propio rey en jaque y evalúa jaque, jaque mate y ahogado.
 *
 * <p>No tiene estado, por lo que una sola instancia puede compartirse.</p>
 */
public class RuleValidator {

    /** Movimientos totalmente legales para la pieza en {@code start} (vacío si no hay pieza). */
    public List<Position> getLegalMoves(Board board, Position start) {
        Piece piece = board.getPiece(start);
        if (piece == null) {
            return List.of();
        }
        return piece.getPseudoLegalMoves(board, start).stream()
                .filter(end -> !wouldBeInCheck(board, start, end, piece.getColor()))
                .toList();
    }

    public boolean isLegalMove(Board board, Position start, Position end) {
        return getLegalMoves(board, start).contains(end);
    }

    /** @return {@code true} si el rey de {@code color} está atacado. */
    public boolean isInCheck(Board board, Color color) {
        Optional<Position> king = board.findKing(color);
        return king.isPresent() && isSquareAttacked(board, king.get(), color.opposite());
    }

    /**
     * @return {@code true} si alguna pieza de {@code attacker} puede mover a {@code target}.
     * Funciona para detectar jaque porque la casilla del rey está ocupada por una pieza
     * enemiga del atacante (los avances del peón a casillas vacías nunca la incluyen).
     */
    public boolean isSquareAttacked(Board board, Position target, Color attacker) {
        for (Map.Entry<Position, Piece> entry : board.getPiecesOf(attacker).entrySet()) {
            if (entry.getValue().getPseudoLegalMoves(board, entry.getKey()).contains(target)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyLegalMove(Board board, Color color) {
        for (Position pos : board.getPiecesOf(color).keySet()) {
            if (!getLegalMoves(board, pos).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Evalúa el estado de la partida para el jugador que debe mover. */
    public GameStatus evaluateStatus(Board board, Color toMove) {
        boolean inCheck = isInCheck(board, toMove);
        boolean canMove = hasAnyLegalMove(board, toMove);
        if (!canMove) {
            return inCheck ? GameStatus.CHECKMATE : GameStatus.STALEMATE;
        }
        return inCheck ? GameStatus.CHECK : GameStatus.IN_PROGRESS;
    }

    /** Simula la jugada en una copia del tablero y verifica si el rey propio queda atacado. */
    private boolean wouldBeInCheck(Board board, Position start, Position end, Color color) {
        Board simulation = board.copy();
        simulation.movePiece(start, end);
        return isInCheck(simulation, color);
    }
}
