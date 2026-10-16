package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GamePlaySessionTest {

    private GamePlaySession session;
    private Game game;

    @BeforeEach
    void setUp() {
        GestorIdioma.setIdioma("pt");
        Fleet fleet = new Fleet();
        fleet.addShip(new Barge(Compass.NORTH, new Position(0, 0)));
        fleet.addShip(new Barge(Compass.NORTH, new Position(9, 9)));
        game = new Game(fleet);
        session = new GamePlaySession();
        session.setGame(game);
        session.beginMove();
    }

    @Test
    void guiMoveUsesExistingRulesAndUpdatesTheBoardAndResults() {
        GamePlaySession.MoveReport report = fire("A1", "B2", "C3");

        assertEquals(1, report.number());
        assertEquals(1, report.hits());
        assertEquals(2, report.misses());
        assertEquals(1, report.sunk());
        assertEquals(1, game.getHits());
        assertEquals(1, game.getSunkShips());
        assertEquals(1, game.getAlienMoves().size());
        GamePlaySession.BoardSnapshot board = session.snapshot();
        assertEquals(2, board.nextMove());
        assertEquals(1, board.remainingShips());
        assertTrue(board.ships()[0][0]);
        assertTrue(board.shots()[0][0]);
        assertTrue(board.shots()[1][1]);
        assertFalse(board.shots()[3][3]);
        assertEquals(report, board.lastMove());
    }

    @Test
    void requiresExactlyThreeTargetsWithoutChangingTheGame() {
        GamePlaySession.BoardSnapshot board = session.snapshot();

        assertThrows(IllegalArgumentException.class, () -> session.fire(
                positions("A1", "B2"), board.generation(), board.nextMove()));

        assertTrue(game.getAlienMoves().isEmpty());
        assertEquals(2, game.getRemainingShips());
        assertNull(session.snapshot().lastMove());
    }

    @Test
    void rejectsDuplicateTargetsWithinAGuiMove() {
        assertThrows(IllegalArgumentException.class, () -> fire("A1", "A1", "B2"));

        assertTrue(game.getAlienMoves().isEmpty());
        assertEquals(0, game.getRepeatedShots());
    }

    @Test
    void rejectsTargetsAlreadySubmittedByEitherInterface() {
        session.fire("A1 B2 C3");
        session.beginMove();

        assertThrows(IllegalArgumentException.class, () -> fire("A1", "D4", "E5"));

        assertEquals(1, game.getAlienMoves().size());
        assertEquals(0, game.getRepeatedShots());
    }

    @Test
    void terminalStillAcceptsAndCountsRepeatedShots() {
        fire("A1", "B2", "C3");
        session.beginMove();

        session.fire("A1 D4 E5");

        assertEquals(2, game.getAlienMoves().size());
        assertEquals(1, game.getRepeatedShots());
        assertEquals(2, session.snapshot().lastMove().number());
    }

    @Test
    void rejectsGuiSelectionsMadeBeforeATerminalMove() {
        GamePlaySession.BoardSnapshot oldBoard = session.snapshot();
        session.fire("A1 B2 C3");

        assertThrows(IllegalArgumentException.class, () -> session.fire(
                positions("D4", "E5", "F6"), oldBoard.generation(), oldBoard.nextMove()));

        assertEquals(1, game.getAlienMoves().size());
    }

    @Test
    void startingAnotherGameClearsHistoryAndRejectsOldSelections() {
        GamePlaySession.BoardSnapshot oldBoard = session.snapshot();
        fire("A1", "B2", "C3");
        Fleet fleet = new Fleet();
        fleet.addShip(new Barge(Compass.NORTH, new Position(9, 9)));
        session.setGame(new Game(fleet));

        GamePlaySession.BoardSnapshot board = session.snapshot();
        assertEquals(1, board.nextMove());
        assertNull(board.lastMove());
        assertFalse(board.shots()[0][0]);
        assertNotEquals(oldBoard.generation(), board.generation());
        assertThrows(IllegalArgumentException.class, () -> session.fire(
                positions("D4", "E5", "F6"), oldBoard.generation(), oldBoard.nextMove()));
    }

    @Test
    void finishingTheGamePreventsAdditionalMovesAndKeepsTheLastResult() {
        fire("A1", "B2", "C3");
        GamePlaySession.MoveReport last = fire("J10", "D4", "E5");

        assertTrue(session.snapshot().finished());
        assertEquals(0, session.snapshot().remainingShips());
        assertEquals(last, session.snapshot().lastMove());
        assertThrows(IllegalStateException.class, () -> fire("F6", "G7", "H8"));
        assertEquals(2, game.getAlienMoves().size());
    }

    @Test
    void guiMovesHaveIndependentDecisionTimers() throws InterruptedException {
        Thread.sleep(140);
        GamePlaySession.MoveReport first = fire("A1", "B2", "C3");
        session.beginMove();
        Thread.sleep(25);
        GamePlaySession.MoveReport second = fire("D4", "E5", "F6");

        assertTrue(first.seconds() >= 0.120);
        assertTrue(second.seconds() >= 0.015);
        assertTrue(second.seconds() < first.seconds());
    }

    @Test
    void liveTimerAdvancesWithoutResettingAndStopsOnSubmission() throws InterruptedException {
        Thread.sleep(80);
        GamePlaySession.TimerSnapshot first = session.timerSnapshot();
        assertTrue(first.running());
        assertTrue(first.seconds() >= 0.060);

        session.beginMove();
        Thread.sleep(30);
        GamePlaySession.TimerSnapshot second = session.timerSnapshot();
        assertTrue(second.seconds() >= first.seconds() + 0.020, "Reading the timer must not restart it");

        GamePlaySession.MoveReport report = fire("A1", "B2", "C3");
        assertFalse(session.timerSnapshot().running());
        assertEquals(0, session.timerSnapshot().seconds());
        assertTrue(report.seconds() >= second.seconds());
        Thread.sleep(20);
        assertEquals(report, session.snapshot().lastMove(), "The submitted duration stays fixed");
    }

    @Test
    void nextMoveAndNewGameHaveFreshLiveTimers() throws InterruptedException {
        Thread.sleep(80);
        GamePlaySession.MoveReport previous = fire("A1", "B2", "C3");
        session.beginMove();

        assertTrue(session.timerSnapshot().running());
        assertTrue(session.timerSnapshot().seconds() < previous.seconds());
        assertEquals(previous, session.snapshot().lastMove());

        Fleet fleet = new Fleet();
        fleet.addShip(new Barge(Compass.NORTH, new Position('A', 1)));
        session.setGame(new Game(fleet));
        assertFalse(session.timerSnapshot().running());
        assertNull(session.snapshot().lastMove());
        session.beginMove();
        assertTrue(session.timerSnapshot().running());
        assertTrue(session.timerSnapshot().seconds() < previous.seconds());
    }

    @Test
    void simulationAndFinishedGamesDoNotRunALiveDecisionTimer() {
        session.beginSimulation();
        assertFalse(session.timerSnapshot().running());
        assertEquals(0, session.timerSnapshot().seconds());
        session.endSimulation();
        assertTrue(session.timerSnapshot().running());

        fire("A1", "B2", "C3");
        session.beginMove();
        fire("J10", "D4", "E5");
        session.beginMove();
        assertTrue(session.snapshot().finished());
        assertFalse(session.timerSnapshot().running());
    }

    @Test
    void snapshotsDoNotExposeMutableBoardState() {
        GamePlaySession.BoardSnapshot snapshot = session.snapshot();
        snapshot.ships()[0][0] = false;
        snapshot.shots()[1][1] = true;

        assertTrue(session.snapshot().ships()[0][0]);
        assertFalse(session.snapshot().shots()[1][1]);
    }

    @Test
    void rejectsManualMovesWhileTheTerminalIsSimulating() {
        session.beginSimulation();

        assertTrue(session.snapshot().simulating());
        assertThrows(IllegalStateException.class, () -> fire("A1", "B2", "C3"));
        assertTrue(game.getAlienMoves().isEmpty());

        session.endSimulation();
        assertFalse(session.snapshot().simulating());
        assertDoesNotThrow(() -> fire("A1", "B2", "C3"));
    }

    private GamePlaySession.MoveReport fire(String... coordinates) {
        GamePlaySession.BoardSnapshot board = session.snapshot();
        return session.fire(positions(coordinates), board.generation(), board.nextMove());
    }

    private List<IPosition> positions(String... coordinates) {
        return java.util.Arrays.stream(coordinates)
                .<IPosition>map(coordinate -> new Position(coordinate.charAt(0), Integer.parseInt(coordinate.substring(1))))
                .toList();
    }
}
