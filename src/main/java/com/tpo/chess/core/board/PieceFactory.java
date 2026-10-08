package com.tpo.chess.core.board;

import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.PieceType;
import com.tpo.chess.core.movement.DiagonalMovement;
import com.tpo.chess.core.movement.KnightMovement;
import com.tpo.chess.core.movement.LinearMovement;
import com.tpo.chess.core.movement.MovementStrategy;
import com.tpo.chess.core.movement.PawnMovement;

import java.util.List;

/**
 * Fábrica de piezas estándar: es el ÚNICO lugar donde se decide qué estrategias
 * componen cada tipo de pieza. Agregar una pieza nueva = agregar una combinación aquí,
 * sin tocar {@link Piece} ni crear subclases (Open/Closed).
 */
public final class PieceFactory {

    // Las estrategias no tienen estado, por lo que se comparten entre piezas.
    private static final MovementStrategy PAWN_MOVE = new PawnMovement();
    private static final MovementStrategy KNIGHT_MOVE = new KnightMovement();
    private static final MovementStrategy LINEAR = new LinearMovement();
    private static final MovementStrategy DIAGONAL = new DiagonalMovement();
    private static final MovementStrategy LINEAR_ONE_STEP = new LinearMovement(1);
    private static final MovementStrategy DIAGONAL_ONE_STEP = new DiagonalMovement(1);

    private PieceFactory() {
    }

    public static Piece create(PieceType type, Color color) {
        List<MovementStrategy> strategies = switch (type) {
            case PAWN -> List.of(PAWN_MOVE);
            case KNIGHT -> List.of(KNIGHT_MOVE);
            case ROOK -> List.of(LINEAR);
            case BISHOP -> List.of(DIAGONAL);
            case QUEEN -> List.of(LINEAR, DIAGONAL);
            case KING -> List.of(LINEAR_ONE_STEP, DIAGONAL_ONE_STEP);
        };
        return new Piece(color, type, strategies);
    }

    public static Piece pawn(Color color) {
        return create(PieceType.PAWN, color);
    }

    public static Piece rook(Color color) {
        return create(PieceType.ROOK, color);
    }

    public static Piece knight(Color color) {
        return create(PieceType.KNIGHT, color);
    }

    public static Piece bishop(Color color) {
        return create(PieceType.BISHOP, color);
    }

    public static Piece queen(Color color) {
        return create(PieceType.QUEEN, color);
    }

    public static Piece king(Color color) {
        return create(PieceType.KING, color);
    }
}
