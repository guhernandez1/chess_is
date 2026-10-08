package com.tpo.chess.adapter.controller;

import com.tpo.chess.adapter.dto.ChessDtos.ErrorDto;
import com.tpo.chess.adapter.dto.ChessDtos.GameStateDto;
import com.tpo.chess.adapter.dto.ChessDtos.LegalMovesDto;
import com.tpo.chess.adapter.dto.ChessDtos.MoveRequest;
import com.tpo.chess.adapter.dto.GameMapper;
import com.tpo.chess.core.Game;
import com.tpo.chess.core.models.MoveResult;
import com.tpo.chess.core.models.Position;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST. Solo traduce HTTP/JSON <-> dominio; toda la lógica
 * de ajedrez vive en {@link Game}.
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final Game game;

    public GameController(Game game) {
        this.game = game;
    }

    @GetMapping
    public GameStateDto getState() {
        return GameMapper.toDto(game);
    }

    @GetMapping("/moves")
    public LegalMovesDto getLegalMoves(@RequestParam("from") String from) {
        Position start = GameMapper.toPosition(from);
        return new LegalMovesDto(GameMapper.toSquare(start),
                game.getLegalMoves(start).stream().map(GameMapper::toSquare).toList());
    }

    @PostMapping("/move")
    public ResponseEntity<?> move(@RequestBody MoveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Falta el cuerpo de la jugada");
        }
        Position from = GameMapper.toPosition(request.from());
        Position to = GameMapper.toPosition(request.to());
        MoveResult result = game.tryMove(from, to);
        if (result.isSuccess()) {
            return ResponseEntity.ok(GameMapper.toDto(game));
        }
        HttpStatus httpStatus = result == MoveResult.GAME_OVER || result == MoveResult.NOT_YOUR_TURN
                ? HttpStatus.CONFLICT : HttpStatus.UNPROCESSABLE_CONTENT;
        return ResponseEntity.status(httpStatus).body(new ErrorDto(result.name(), describe(result)));
    }

    @PostMapping("/reset")
    public GameStateDto reset() {
        game.reset();
        return GameMapper.toDto(game);
    }

    private static String describe(MoveResult result) {
        return switch (result) {
            case GAME_OVER -> "La partida ya terminó. Reiniciá para jugar otra.";
            case OUT_OF_BOARD -> "La casilla está fuera del tablero.";
            case NO_PIECE -> "No hay ninguna pieza en la casilla de origen.";
            case NOT_YOUR_TURN -> "No es el turno de esa pieza.";
            case ILLEGAL_MOVE -> "Movimiento ilegal para esa pieza (o deja a tu rey en jaque).";
            case SUCCESS -> "OK";
        };
    }
}
