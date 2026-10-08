package com.tpo.chess.core;

import com.tpo.chess.core.board.Board;
import com.tpo.chess.core.models.Color;
import com.tpo.chess.core.models.GameStatus;
import com.tpo.chess.core.models.Move;
import com.tpo.chess.core.models.MoveResult;
import com.tpo.chess.core.models.PieceType;
import com.tpo.chess.core.models.Position;
import com.tpo.chess.core.rules.RuleValidator;
import org.junit.jupiter.api.Test;

import static com.tpo.chess.core.TestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de la fachada {@link Game}. JUnit 5 puro: no se levanta Spring.
 */
class GameTest {

    @Test
    void newGameStartsWithWhiteToMove() {
        Game game = new Game();
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(32, game.getBoard().getAllPieces().size());
        assertTrue(game.getHistory().isEmpty());
        assertTrue(game.getWinner().isEmpty());
    }

    @Test
    void legalMoveIsExecutedAndTurnPasses() {
        Game game = new Game();
        assertTrue(game.playTurn(sq("e2"), sq("e4")));

        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertTrue(game.getBoard().isEmpty(sq("e2")));
        assertEquals(PieceType.PAWN, game.getBoard().getPiece(sq("e4")).getPieceType());
        assertEquals(1, game.getHistory().size());
    }

    @Test
    void cannotMoveOpponentPieces() {
        Game game = new Game();
        assertEquals(MoveResult.NOT_YOUR_TURN, game.tryMove(sq("e7"), sq("e5")));
        assertFalse(game.playTurn(sq("e7"), sq("e5")));
        assertEquals(Color.WHITE, game.getCurrentTurn());
    }

    @Test
    void rejectsIllegalAndInvalidMoves() {
        Game game = new Game();
        assertEquals(MoveResult.ILLEGAL_MOVE, game.tryMove(sq("e2"), sq("e5")));
        assertEquals(MoveResult.ILLEGAL_MOVE, game.tryMove(sq("a1"), sq("a3")), "La torre está bloqueada");
        assertEquals(MoveResult.NO_PIECE, game.tryMove(sq("e4"), sq("e5")));
        assertEquals(MoveResult.OUT_OF_BOARD, game.tryMove(sq("e2"), new Position(8, 4)));
        assertTrue(game.getHistory().isEmpty());
        assertEquals(Color.WHITE, game.getCurrentTurn());
    }

    @Test
    void foolsMateEndsTheGame() {
        Game game = new Game();
        assertTrue(game.playTurn(sq("f2"), sq("f3")));
        assertTrue(game.playTurn(sq("e7"), sq("e5")));
        assertTrue(game.playTurn(sq("g2"), sq("g4")));
        assertTrue(game.playTurn(sq("d8"), sq("h4")));

        assertEquals(GameStatus.CHECKMATE, game.getStatus());
        assertEquals(Color.BLACK, game.getWinner().orElseThrow());
        assertEquals(MoveResult.GAME_OVER, game.tryMove(sq("a2"), sq("a3")));
        assertTrue(game.getLegalMoves(sq("a2")).isEmpty());
    }

    @Test
    void captureIsRecordedInHistory() {
        Game game = new Game();
        game.playTurn(sq("e2"), sq("e4"));
        game.playTurn(sq("d7"), sq("d5"));
        assertTrue(game.playTurn(sq("e4"), sq("d5")));

        Move last = game.getHistory().get(2);
        assertTrue(last.isCapture());
        assertEquals(PieceType.PAWN, last.captured());
        assertEquals(31, game.getBoard().getAllPieces().size());
    }

    @Test
    void pawnPromotesToQueenAndGivesCheck() {
        Board board = boardWith("a7", wP(), "e1", wK(), "e8", bK());
        Game game = new Game(board, Color.WHITE, new RuleValidator());

        assertTrue(game.playTurn(sq("a7"), sq("a8")));
        assertEquals(PieceType.QUEEN, game.getBoard().getPiece(sq("a8")).getPieceType());
        assertTrue(game.getHistory().get(0).promotion());
        assertEquals(GameStatus.CHECK, game.getStatus());
    }

    @Test
    void cannotMakeAMoveThatLeavesOwnKingInCheck() {
        Board board = boardWith("e1", wK(), "e2", wB(), "e8", bR(), "a8", bK());
        Game game = new Game(board, Color.WHITE, new RuleValidator());
        assertEquals(MoveResult.ILLEGAL_MOVE, game.tryMove(sq("e2"), sq("d3")));
    }

    @Test
    void exposedBoardIsADefensiveCopy() {
        Game game = new Game();
        game.getBoard().removePiece(sq("e1"));
        assertFalse(game.getBoard().isEmpty(sq("e1")));
    }

    @Test
    void resetRestoresInitialPosition() {
        Game game = new Game();
        game.playTurn(sq("e2"), sq("e4"));
        game.reset();

        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertTrue(game.getHistory().isEmpty());
        assertFalse(game.getBoard().isEmpty(sq("e2")));
    }
}
