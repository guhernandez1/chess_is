package com.tpo.chess.adapter.dto;

import java.util.List;

/**
 * DTOs expuestos por la API REST. Viven en el adaptador para que el formato de
 * transporte (notación algebraica, strings) no contamine el dominio.
 */
public final class ChessDtos {

    private ChessDtos() {
    }

    /** Pedido de jugada en notación algebraica, ej. {@code {"from":"e2","to":"e4"}}. */
    public record MoveRequest(String from, String to) {
    }

    public record PieceDto(String square, String color, String type) {
    }

    public record MoveDto(String from, String to, String color, String piece,
                          String captured, boolean promotion) {
    }

    public record GameStateDto(List<PieceDto> pieces, String turn, String status,
                               String winner, List<MoveDto> history) {
    }

    public record LegalMovesDto(String from, List<String> moves) {
    }

    public record ErrorDto(String code, String message) {
    }
}
