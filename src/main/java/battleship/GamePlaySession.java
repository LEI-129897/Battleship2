package battleship;

import org.apache.commons.lang3.time.StopWatch;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;

/** Shares game state and the decision timer between the terminal and JavaFX. */
public class GamePlaySession {

    private IGame game;
    private StopWatch moveTimer;
    private MoveReport lastMove;
    private int generation;
    private boolean simulating;

    public synchronized void setGame(IGame game) {
        this.game = Objects.requireNonNull(game);
        moveTimer = null;
        lastMove = null;
        simulating = false;
        generation++;
    }

    public synchronized boolean hasGame() {
        return game != null;
    }

    public synchronized boolean isGame(IGame candidate) {
        return game == candidate;
    }

    public synchronized void withGame(Consumer<IGame> action) {
        if (game != null) {
            action.accept(game);
        }
    }

    public synchronized void beginMove() {
        if (game != null && game.getRemainingShips() > 0 && !simulating && moveTimer == null) {
            moveTimer = StopWatch.createStarted();
        }
    }

    /** Reads the live stopwatch without stopping or restarting the current move. */
    public synchronized TimerSnapshot timerSnapshot() {
        boolean running = moveTimer != null && moveTimer.isStarted() && !moveTimer.isStopped();
        double seconds = running ? moveTimer.getDuration().toNanos() / 1_000_000_000.0 : 0;
        return new TimerSnapshot(seconds, running);
    }

    /** Submits GUI coordinates atomically, including validation of stale selections. */
    public synchronized MoveReport fire(List<IPosition> shots, int expectedGeneration, int expectedMove) {
        ensurePlayable();
        if (generation != expectedGeneration || game.getAlienMoves().size() + 1 != expectedMove) {
            throw new IllegalArgumentException(GestorIdioma.getMensagem("guiMoveChanged"));
        }
        if (shots == null || shots.size() != Game.NUMBER_SHOTS
                || shots.stream().anyMatch(position -> position == null || !position.isInside())) {
            throw new IllegalArgumentException(GestorIdioma.getMensagemFormatada("guiSelectShots", Game.NUMBER_SHOTS));
        }
        for (int index = 0; index < shots.size(); index++) {
            IPosition position = shots.get(index);
            if (shots.subList(0, index).contains(position)
                    || game.getAlienMoves().stream().anyMatch(move -> move.getShots().contains(position))) {
                throw new IllegalArgumentException(GestorIdioma.getMensagem("guiAlreadyShot"));
            }
        }
        List<IPosition> submittedShots = List.copyOf(shots);
        return submit(() -> game.fireShots(submittedShots));
    }

    /** Keeps the terminal's existing parser and repeated/outside shot rules. */
    public synchronized MoveReport fire(String input) {
        ensurePlayable();
        return submit(() -> {
            try (Scanner scanner = new Scanner(input + "\n")) {
                game.readEnemyFire(scanner);
            }
        });
    }

    private void ensurePlayable() {
        if (game == null || game.getRemainingShips() == 0) {
            throw new IllegalStateException(GestorIdioma.getMensagem("guiFinished"));
        }
        if (simulating) {
            throw new IllegalStateException(GestorIdioma.getMensagem("guiSimulating"));
        }
    }

    private MoveReport submit(Runnable fire) {
        beginMove();
        moveTimer.stop();
        double seconds = moveTimer.getDuration().toNanos() / 1_000_000_000.0;
        try {
            fire.run();
        } catch (RuntimeException exception) {
            moveTimer = null;
            beginMove();
            throw exception;
        }
        IMove move = game.getAlienMoves().getLast();
        int hits = 0;
        int misses = 0;
        int sunk = 0;
        for (IGame.ShotResult result : move.getShotResults()) {
            if (result.valid() && !result.repeated()) {
                if (result.ship() == null) {
                    misses++;
                } else {
                    hits++;
                    if (result.sunk()) {
                        sunk++;
                    }
                }
            }
        }
        lastMove = new MoveReport(move.getNumber(), seconds, hits, misses, sunk);
        System.out.printf(Locale.ROOT, "Tempo da jogada nº%d: %.3f segundos.%n", move.getNumber(), seconds);
        moveTimer = null;
        return lastMove;
    }

    public synchronized void beginSimulation() {
        ensurePlayable();
        simulating = true;
        moveTimer = null;
    }

    public synchronized void simulateMove() {
        game.randomEnemyFire();
        lastMove = null;
    }

    public synchronized void endSimulation() {
        simulating = false;
        beginMove();
    }

    /** Copies the board under the same lock used to submit moves. */
    public synchronized BoardSnapshot snapshot() {
        boolean[][] ships = new boolean[Game.BOARD_SIZE][Game.BOARD_SIZE];
        boolean[][] shots = new boolean[Game.BOARD_SIZE][Game.BOARD_SIZE];
        if (game != null) {
            for (IShip ship : game.getMyFleet().getShips()) {
                for (IPosition position : ship.getPositions()) {
                    ships[position.getRow()][position.getColumn()] = true;
                }
            }
            for (IMove move : game.getAlienMoves()) {
                for (IPosition position : move.getShots()) {
                    if (position.isInside()) {
                        shots[position.getRow()][position.getColumn()] = true;
                    }
                }
            }
        }
        return new BoardSnapshot(generation, game == null ? 1 : game.getAlienMoves().size() + 1,
                game == null ? 0 : game.getRemainingShips(), game != null, simulating, ships, shots, lastMove);
    }

    public record MoveReport(int number, double seconds, int hits, int misses, int sunk) {}

    public record TimerSnapshot(double seconds, boolean running) {}

    public record BoardSnapshot(int generation, int nextMove, int remainingShips, boolean hasGame,
                                boolean simulating, boolean[][] ships, boolean[][] shots, MoveReport lastMove) {
        public boolean finished() {
            return hasGame && remainingShips == 0;
        }
    }
}
