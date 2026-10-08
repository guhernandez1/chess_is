package com.tpo.chess.adapter.dto;

import com.tpo.chess.core.models.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameMapperTest {

    @Test
    void convertsAlgebraicNotationBothWays() {
        assertEquals(new Position(0, 0), GameMapper.toPosition("a1"));
        assertEquals(new Position(1, 4), GameMapper.toPosition("E2"));
        assertEquals(new Position(7, 7), GameMapper.toPosition(" h8 "));
        assertEquals("e4", GameMapper.toSquare(new Position(3, 4)));
    }

    @Test
    void rejectsInvalidSquares() {
        assertThrows(IllegalArgumentException.class, () -> GameMapper.toPosition("i1"));
        assertThrows(IllegalArgumentException.class, () -> GameMapper.toPosition("a9"));
        assertThrows(IllegalArgumentException.class, () -> GameMapper.toPosition(null));
    }
}
