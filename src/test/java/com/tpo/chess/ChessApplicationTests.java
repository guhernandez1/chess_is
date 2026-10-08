package com.tpo.chess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integración del ADAPTADOR (sí levanta Spring). Las reglas del juego se testean
 * aparte, en {@code core}, sin framework.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ChessApplicationTests {

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void resetGame() throws Exception {
        mvc.perform(post("/api/game/reset")).andExpect(status().isOk());
    }

    @Test
    void exposesInitialState() throws Exception {
        mvc.perform(get("/api/game"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turn").value("WHITE"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.pieces.length()").value(32));
    }

    @Test
    void listsLegalMoves() throws Exception {
        mvc.perform(get("/api/game/moves").param("from", "b1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moves.length()").value(2));
    }

    @Test
    void playsALegalMove() throws Exception {
        mvc.perform(post("/api/game/move").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e2\",\"to\":\"e4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turn").value("BLACK"))
                .andExpect(jsonPath("$.history[0].to").value("e4"));
    }

    @Test
    void rejectsIllegalMove() throws Exception {
        mvc.perform(post("/api/game/move").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e2\",\"to\":\"e5\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("ILLEGAL_MOVE"));
    }

    @Test
    void rejectsInvalidNotation() throws Exception {
        mvc.perform(post("/api/game/move").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"z9\",\"to\":\"e4\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
