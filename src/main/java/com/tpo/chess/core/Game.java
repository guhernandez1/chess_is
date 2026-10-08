package com.tpo.chess.core;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.board.BoardFactory;
import com.tpo.chess.core.board.PieceFactory;
import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.GameStatus;
import com.tpo.chess.core.models.Move;
import com.tpo.chess.core.models.MoveResult;
import com.tpo.chess.core.models.Piece;
import com.tpo.chess.core.models.PieceType;
import com.tpo.chess.core.models.Position;
import com.tpo.chess.core.movement.PawnMovement;
import com.tpo.chess.core.rules.RuleValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Fachada del núcleo: orquesta tablero, turnos y validación de reglas.
 *
 * <p>Java puro: no tiene anotaciones de Spring. El adaptador lo instancia mediante
 * {@code @Configuration}/{@code @Bean}.</p>
 *
 * <p>Los métodos públicos son {@code synchronized} porque el adaptador web expone una
 * única partida compartida que puede recibir requests concurrentes.</p>
 */
public class Game {

    private final RuleValidator validator;
    private Board board;
    private Color currentTurn;
    private GameStatus status;
    private final List<Move> history = new ArrayList<>();

    /** Partida estándar con un validador por defecto. */
    public Game() {
        this(new RuleValidator());
    }

    /** Partida estándar con el validador inyectado. */
    public Game(RuleValidator validator) {
        this(BoardFactory.createStandardBoard(), Color.WHITE, validator);
    }

    /** Partida desde una posición arbitraria (útil para tests y problemas de ajedrez). */
    public Game(Board board, Color startingTurn, RuleValidator validator) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.board = Objects.requireNonNull(board, "board").copy();
        this.currentTurn = Objects.requireNonNull(startingTurn, "startingTurn");
        this.status = validator.evaluateStatus(this.board, currentTurn);
    }

    /**
     * Intenta ejecutar una jugada del jugador con el turno.
     *
     * @return {@code true} si la jugada era legal y se ejecutó.
     */
    public boolean playTurn(Position start, Position end) {
        return tryMove(start, end).isSuccess();
    }

    /** Igual que {@link #playTurn} pero informando el motivo de un rechazo. */
    public synchronized MoveResult tryMove(Position start, Position end) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (status.isFinished()) {
            return MoveResult.GAME_OVER;
        }
        if (!start.isOnBoard() || !end.isOnBoard()) {
            return MoveResult.OUT_OF_BOARD;
        }
        Piece piece = board.getPiece(start);
        if (piece == null) {
            return MoveResult.NO_PIECE;
        }
        if (piece.getColor() != currentTurn) {
            return MoveResult.NOT_YOUR_TURN;
        }
        if (!validator.isLegalMove(board, start, end)) {
            return MoveResult.ILLEGAL_MOVE;
        }

        Piece captured = board.movePiece(start, end);
        boolean promotion = applyPromotionIfNeeded(piece, end);
        history.add(new Move(start, end, piece.getColor(), piece.getPieceType(),
                captured == null ? null : captured.getPieceType(), promotion));

        currentTurn = currentTurn.opposite();
        status = validator.evaluateStatus(board, currentTurn);
        return MoveResult.SUCCESS;
    }

    /** Coronación automática a Reina (elección de pieza fuera del alcance del MVP). */
    private boolean applyPromotionIfNeeded(Piece piece, Position end) {
        if (piece.getPieceType() == PieceType.PAWN && end.row() == PawnMovement.promotionRow(piece.getColor())) {
            board.placePiece(end, PieceFactory.queen(piece.getColor()));
            return true;
        }
        return false;
    }

    /** Movimientos legales de la pieza en {@code pos}; vacío si la partida terminó. */
    public synchronized List<Position> getLegalMoves(Position pos) {
        if (status.isFinished() || !pos.isOnBoard()) {
            return List.of();
        }
        return validator.getLegalMoves(board, pos);
    }

    /** Reinicia a la posición inicial estándar. */
    public synchronized void reset() {
        board = BoardFactory.createStandardBoard();
        currentTurn = Color.WHITE;
        history.clear();
        status = validator.evaluateStatus(board, currentTurn);
    }

    /** @return una copia del tablero (el estado interno no puede modificarse desde afuera). */
    public synchronized Board getBoard() {
        return board.copy();
    }

    public synchronized Color getCurrentTurn() {
        return currentTurn;
    }

    public synchronized GameStatus getStatus() {
        return status;
    }

    /** @return el ganador si hubo jaque mate. */
    public synchronized Optional<Color> getWinner() {
        return status == GameStatus.CHECKMATE ? Optional.of(currentTurn.opposite()) : Optional.empty();
    }

    public synchronized List<Move> getHistory() {
        return List.copyOf(history);
    }
}
