package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link Move}.
 * Author: luiberna
 * Date: 2026-11-18 03:12 UTC
 * Cyclomatic Complexity (McCabe/Sonar-style):
 * - constructor: 1
 * - toString(): 1
 * - getNumber(): 1
 * - getShots(): 1
 * - getShotResults(): 1
 * - processEnemyFire(): 34
 * - createObjectMapper(): 1
 */
class MoveTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private Move move;

    @BeforeEach
    void setUp() {
        move = new Move(
                7,
                new ArrayList<>(List.of(new Position(0, 0))),
                new ArrayList<>(List.of(shot(true, false, null, false))));
    }

    @AfterEach
    void tearDown() {
        move = null;
    }

    @Test
    @DisplayName("Constructor stores the move number, shots, and results")
    void constructor() {
        List<IPosition> shots = List.of(new Position(2, 3));
        List<IGame.ShotResult> results = List.of(shot(true, false, null, false));
        Move created = new Move(4, shots, results);

        assertAll(
                () -> assertEquals(4, created.getNumber(), "Error: expected move number 4"),
                () -> assertEquals(shots, created.getShots(), "Error: expected the constructor's shots"),
                () -> assertEquals(results, created.getShotResults(), "Error: expected the constructor's results"));
    }

    @Test
    @DisplayName("toString includes the move number and collection sizes")
    void toStringTest() {
        assertEquals("Move{number=7, shots=1, results=1}", move.toString(),
                "Error: expected the move summary with its number and collection sizes");
    }

    @Test
    @DisplayName("getNumber returns the move number")
    void getNumber() {
        assertEquals(7, move.getNumber(), "Error: expected move number 7");
    }

    @Test
    @DisplayName("getShots returns the move shots")
    void getShots() {
        assertAll(
                () -> assertEquals(1, move.getShots().size(), "Error: expected one shot"),
                () -> assertEquals(new Position(0, 0), move.getShots().get(0),
                        "Error: expected the configured shot position"));
    }

    @Test
    @DisplayName("getShotResults returns the shot results")
    void getShotResults() {
        assertAll(
                () -> assertEquals(1, move.getShotResults().size(), "Error: expected one shot result"),
                () -> assertEquals(shot(true, false, null, false), move.getShotResults().get(0),
                        "Error: expected the configured shot result"));
    }

    @Test
    @DisplayName("processEnemyFire1: empty results without verbose output")
    void processEnemyFire1() {
        assertSummary(process(false), 0, 3, 0, 0, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire2: empty results with verbose output")
    void processEnemyFire2() {
        assertSummary(process(true), 0, 3, 0, 0, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire3: invalid shot is ignored")
    void processEnemyFire3() {
        assertSummary(process(false, shot(false, false, null, false)), 0, 3, 0, 0, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire4: invalid shot is ignored in verbose mode")
    void processEnemyFire4() {
        assertSummary(process(true, shot(false, false, null, false)), 0, 3, 0, 0, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire5: valid shot into water is counted as a miss")
    void processEnemyFire5() {
        assertSummary(process(false, shot(true, false, null, false)), 1, 2, 0, 1, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire6: repeated shot is counted separately")
    void processEnemyFire6() {
        assertSummary(process(false, shot(true, true, null, false)), 0, 2, 1, 0, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire7: hit on a ship that remains afloat")
    void processEnemyFire7() {
        assertSummary(process(false, shot(true, false, barge(), false)), 1, 2, 0, 0, 0, 1);
    }

    @Test
    @DisplayName("processEnemyFire8: hit that sinks a ship")
    void processEnemyFire8() {
        assertSummary(process(false, shot(true, false, barge(), true)), 1, 2, 0, 0, 1, 0);
    }

    @Test
    @DisplayName("processEnemyFire9: invalid, valid, and repeated results are distinguished")
    void processEnemyFire9() {
        assertSummary(process(false,
                shot(false, false, null, false),
                shot(true, false, null, false),
                shot(true, true, null, false)), 1, 1, 1, 1, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire10: verbose output handles one repeated shot")
    void processEnemyFire10() {
        assertSummary(process(true, shot(true, true, null, false)), 0, 2, 1, 0, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire11: verbose output handles multiple repeated shots")
    void processEnemyFire11() {
        assertSummary(process(true,
                shot(true, false, null, false),
                shot(true, true, null, false),
                shot(true, true, null, false)), 1, 0, 2, 1, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire12: verbose output handles one miss")
    void processEnemyFire12() {
        assertSummary(process(true, shot(true, false, null, false)), 1, 2, 0, 1, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire13: verbose output handles multiple misses")
    void processEnemyFire13() {
        assertSummary(process(true,
                shot(true, false, null, false),
                shot(true, false, null, false)), 2, 1, 0, 2, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire14: verbose output handles three misses")
    void processEnemyFire14() {
        assertSummary(process(true,
                shot(true, false, null, false),
                shot(true, false, null, false),
                shot(true, false, null, false)), 3, 0, 0, 3, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire15: verbose output reports a single ship hit")
    void processEnemyFire15() {
        assertSummary(process(true, shot(true, false, barge(), false)), 1, 2, 0, 0, 0, 1);
    }

    @Test
    @DisplayName("processEnemyFire16: verbose output reports multiple hits on one afloat ship")
    void processEnemyFire16() {
        assertSummary(process(true,
                shot(true, false, barge(), false),
                shot(true, false, barge(), false)), 2, 1, 0, 0, 0, 1);
    }

    @Test
    @DisplayName("processEnemyFire17: verbose output reports one sunk ship")
    void processEnemyFire17() {
        assertSummary(process(true, shot(true, false, barge(), true)), 1, 2, 0, 0, 1, 0);
    }

    @Test
    @DisplayName("processEnemyFire18: verbose output reports multiple sinkings of one ship type")
    void processEnemyFire18() {
        assertSummary(process(true,
                shot(true, false, barge(), true),
                shot(true, false, barge(), true)), 2, 1, 0, 0, 1, 0);
    }

    @Test
    @DisplayName("processEnemyFire19: verbose output reports sunk ships of different types")
    void processEnemyFire19() {
        assertSummary(process(true,
                shot(true, false, barge(), true),
                shot(true, false, caravel(), true)), 2, 1, 0, 0, 2, 0);
    }

    @Test
    @DisplayName("processEnemyFire20: reports both sunk ships and hits on afloat ships")
    void processEnemyFire20() {
        assertSummary(process(true,
                shot(true, false, barge(), true),
                shot(true, false, caravel(), false)), 2, 1, 0, 0, 1, 1);
    }

    @Test
    @DisplayName("processEnemyFire21: mixed outcomes exercise the result loop")
    void processEnemyFire21() {
        assertSummary(process(true,
                shot(false, false, null, false),
                shot(true, false, barge(), false),
                shot(true, true, null, false)), 1, 1, 1, 0, 0, 1);
    }

    @Test
    @DisplayName("processEnemyFire22: a miss and repeated shot are both reported")
    void processEnemyFire22() {
        assertSummary(process(true,
                shot(true, false, null, false),
                shot(true, true, null, false)), 1, 1, 1, 1, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire23: all three slots contain valid or repeated shots")
    void processEnemyFire23() {
        assertSummary(process(true,
                shot(true, false, barge(), true),
                shot(true, false, null, false),
                shot(true, true, null, false)), 2, 0, 1, 1, 1, 0);
    }

    @Test
    @DisplayName("processEnemyFire24: repeated shots with no valid shots take the special verbose path")
    void processEnemyFire24() {
        assertSummary(process(true,
                shot(true, true, null, false),
                shot(true, true, null, false)), 0, 1, 2, 0, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire25: invalid shots leave outside shots to report")
    void processEnemyFire25() {
        assertSummary(process(true,
                shot(false, false, null, false),
                shot(false, false, null, false)), 0, 3, 0, 0, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire26: multiple outside shots use the plural verbose path")
    void processEnemyFire26() {
        assertSummary(process(true), 0, 3, 0, 0, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire27: non-empty verbose output separates outside-shot details")
    void processEnemyFire27() {
        assertSummary(process(true, shot(true, false, null, false)), 1, 2, 0, 1, 0, 0);
    }

    @Test
    @DisplayName("processEnemyFire28: afloat-ship hits populate the hits collection")
    void processEnemyFire28() {
        assertSummary(process(true, shot(true, false, caravel(), false)), 1, 2, 0, 0, 0, 1);
    }

    @Test
    @DisplayName("processEnemyFire29: sunk and afloat hits of the same type share the hit count")
    void processEnemyFire29() {
        assertSummary(process(true,
                shot(true, false, barge(), true),
                shot(true, false, barge(), false)), 2, 1, 0, 0, 1, 0);
    }

    @Test
    @DisplayName("processEnemyFire30: hits on different afloat ship types are both serialized")
    void processEnemyFire30() {
        assertSummary(process(true,
                shot(true, false, barge(), false),
                shot(true, false, caravel(), false)), 2, 1, 0, 0, 0, 2);
    }

    @Test
    @DisplayName("processEnemyFire31: multiple sunk ships are serialized")
    void processEnemyFire31() {
        assertSummary(process(true,
                shot(true, false, barge(), true),
                shot(true, false, caravel(), true)), 2, 1, 0, 0, 2, 0);
    }

    @Test
    @DisplayName("processEnemyFire32: misses prevent trimming the verbose ship summary")
    void processEnemyFire32() {
        assertSummary(process(true,
                shot(true, false, barge(), true),
                shot(true, false, null, false)), 2, 1, 0, 1, 1, 0);
    }

    @Test
    @DisplayName("processEnemyFire33: valid shots and repeated shots share the verbose summary")
    void processEnemyFire33() {
        assertSummary(process(true,
                shot(true, false, barge(), false),
                shot(true, true, null, false),
                shot(false, false, null, false)), 1, 1, 1, 0, 0, 1);
    }

    @Test
    @DisplayName("processEnemyFire34: mixed hit, miss, repeat, and invalid paths")
    void processEnemyFire34() {
        assertSummary(process(true,
                shot(true, false, barge(), true),
                shot(true, false, null, false),
                shot(true, true, null, false),
                shot(false, false, null, false)), 2, 0, 1, 1, 1, 0);
    }

    @Test
    @DisplayName("processEnemyFire propagates JSON serialization failures")
    void processEnemyFireSerializationFailure() {
        Move failingMove = new Move(7, List.of(), List.of()) {
            @Override
            protected ObjectMapper createObjectMapper() {
                return new ObjectMapper() {
                    @Override
                    public String writeValueAsString(Object value) throws JsonProcessingException {
                        throw new JsonProcessingException("Forced serialization failure") {};
                    }
                };
            }
        };

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> failingMove.processEnemyFire(false),
                "Error: expected a serialization failure to be wrapped in RuntimeException");
        assertAll(
                () -> assertEquals("Erro ao serializar o JSON dos resultados da jogada", exception.getMessage(),
                        "Error: expected the serialization error message"),
                () -> assertInstanceOf(JsonProcessingException.class, exception.getCause(),
                        "Error: expected the JSON processing exception as the cause"));
    }

    private JsonNode process(boolean verbose, IGame.ShotResult... results) {
        move = new Move(7, List.of(), List.of(results));
        try {
            return JSON.readTree(move.processEnemyFire(verbose));
        } catch (JsonProcessingException exception) {
            throw new AssertionError("Error: expected processEnemyFire to return valid JSON", exception);
        }
    }

    private static void assertSummary(
            JsonNode json,
            int validShots,
            int outsideShots,
            int repeatedShots,
            int missedShots,
            int sunkBoatCount,
            int boatHitCount) {
        assertAll("JSON shot summary",
                () -> assertEquals(validShots, json.path("validShots").asInt(-1),
                        "Error: expected the valid-shot count"),
                () -> assertEquals(outsideShots, json.path("outsideShots").asInt(-1),
                        "Error: expected the outside-shot count"),
                () -> assertEquals(repeatedShots, json.path("repeatedShots").asInt(-1),
                        "Error: expected the repeated-shot count"),
                () -> assertEquals(missedShots, json.path("missedShots").asInt(-1),
                        "Error: expected the missed-shot count"),
                () -> assertEquals(sunkBoatCount, json.path("sunkBoats").size(),
                        "Error: expected the serialized sunk-boat count"),
                () -> assertEquals(boatHitCount, json.path("hitsOnBoats").size(),
                        "Error: expected the serialized afloat-boat hit count"));
    }

    private static IGame.ShotResult shot(boolean valid, boolean repeated, IShip ship, boolean sunk) {
        return new IGame.ShotResult(valid, repeated, ship, sunk);
    }

    private static IShip barge() {
        return new Barge(Compass.NORTH, new Position(0, 0));
    }

    private static IShip caravel() {
        return new Caravel(Compass.NORTH, new Position(1, 1));
    }
}
