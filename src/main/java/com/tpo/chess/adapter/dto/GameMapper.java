package com.tpo.chess.adapter.dto;

import com.tpo.chess.adapter.dto.ChessDtos.GameStateDto;
import com.tpo.chess.adapter.dto.ChessDtos.MoveDto;
import com.tpo.chess.adapter.dto.ChessDtos.PieceDto;
import com.tpo.chess.core.Game;
import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.Move;
import com.tpo.chess.core.models.Position;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Traduce entre el modelo del dominio y los DTOs/notación algebraica.
 */
public final class GameMapper {

    private GameMapper() {
    }

    /** "e2" -> Position(1, 4). */
    public static Position toPosition(String square) {
        if (square == null || !square.trim().matches("(?i)[a-h][1-8]")) {
            throw new IllegalArgumentException("Casilla inválida: '" + square + "'. Usar notación algebraica, ej. e2");
        }
        String s = square.trim().toLowerCase(Locale.ROOT);
        return new Position(s.charAt(1) - '1', s.charAt(0) - 'a');
    }

    /** Position(1, 4) -> "e2". */
    public static String toSquare(Position pos) {
        return "" + (char) ('a' + pos.col()) + (char) ('1' + pos.row());
    }

    public static GameStateDto toDto(Game game) {
        List<PieceDto> pieces = game.getBoard().getAllPieces().entrySet().stream()
                .sorted(Comparator.comparing((java.util.Map.Entry<Position, ?> e) -> e.getKey().row())
                        .thenComparing(e -> e.getKey().col()))
                .map(e -> new PieceDto(toSquare(e.getKey()), e.getValue().getColor().name(),
                        e.getValue().getPieceType().name()))
                .toList();
        List<MoveDto> history = game.getHistory().stream().map(GameMapper::toDto).toList();
        String winner = game.getWinner().map(Color::name).orElse(null);
        return new GameStateDto(pieces, game.getCurrentTurn().name(), game.getStatus().name(), winner, history);
    }

    private static MoveDto toDto(Move move) {
        return new MoveDto(toSquare(move.from()), toSquare(move.to()), move.color().name(),
                move.pieceType().name(), move.captured() == null ? null : move.captured().name(),
                move.promotion());
    }
}
