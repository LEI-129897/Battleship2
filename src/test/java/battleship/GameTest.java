package battleship;

import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 */
public class GameTest {

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet()); // Assuming Fleet is a concrete implementation of IFleet
	}

	@AfterEach
	void tearDown() {
		game = null;
	}

	@Test
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}

	@Test
	@DisplayName("Test fireSingleShot - Miss Water (Ship null branch)")
	void fireSingleShot1() {
		IGame.ShotResult result = game.fireSingleShot(new Position(5, 5), false);
		assertAll("Miss water checks",
				() -> assertTrue(result.valid(), "Error: Shot should be valid."),
				() -> assertFalse(result.repeated(), "Error: Shot should not be repeated."),
				() -> assertNull(result.ship(), "Error: No ship should be hit."),
				() -> assertFalse(result.sunk(), "Error: No ship should be sunk.")
		);
	}

	@Test
	@DisplayName("Test fireSingleShot - Hit without sinking (stillFloating true branch)")
	void fireSingleShot2() {
		Ship frigate = new Frigate(Compass.NORTH, new Position(2, 2));
		game.getMyFleet().addShip(frigate);

		IGame.ShotResult result = game.fireSingleShot(new Position(2, 2), false);
		assertAll("Hit without sink checks",
				() -> assertTrue(result.valid(), "Error: Shot should be valid."),
				() -> assertNotNull(result.ship(), "Error: Should hit the frigate."),
				() -> assertFalse(result.sunk(), "Error: Frigate should still be floating after 1 hit."),
				() -> assertEquals(1, game.getHits(), "Error: Hits should be 1."),
				() -> assertEquals(0, game.getSunkShips(), "Error: Sunk ships should be 0.")
		);
	}

	@Test
	@DisplayName("Test fireShots - Valid Execution")
	void fireShots1() {
		List<IPosition> positions = List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2));
		assertDoesNotThrow(() -> game.fireShots(positions), "Error: fireShots should execute successfully.");
		assertEquals(1, game.getAlienMoves().size(), "Error: Alien moves size should be 1.");
	}

	@Test
	@DisplayName("Test fireShots - Too many shots exception branch")
	void fireShots2() {
		List<IPosition> tooManyShots = List.of(
				new Position(0, 0), new Position(0, 1), new Position(0, 2), new Position(0, 3)
		);
		assertThrows(IllegalArgumentException.class, () -> {
			game.fireShots(tooManyShots);
		}, "Error: Should throw exception for invalid number of shots.");
	}

	@Test
	@DisplayName("Test randomEnemyFire execution")
	void randomEnemyFire1() {
		String json = game.randomEnemyFire();
		assertNotNull(json, "Error: randomEnemyFire should return JSON.");
		assertEquals(1, game.getAlienMoves().size(), "Error: Alien moves should increase.");
	}

	@Test
	@DisplayName("Test randomEnemyFire - Else Branch (Low candidate positions)")
	void randomEnemyFire2() {
		for (int r = 0; r < 10; r++) {
			for (int c = 0; c < 10; c++) {
				game.getMyFleet().addShip(new Barge(Compass.NORTH, new Position(r, c)));
			}
		}
		game.getMyFleet().getShips().get(0).getPositions().get(0).shoot();

		String json = game.randomEnemyFire();
		assertNotNull(json, "Error: randomEnemyFire should execute else branch successfully.");
	}

	@Test
	@DisplayName("Test randomEnemyFire - Else branch when fewer than 3 positions remain")
	void randomEnemyFire3() {
		List<IPosition> batch = new java.util.ArrayList<>();
		for (int r = 0; r < 10; r++) {
			for (int c = 0; c < 10; c++) {
				if (r == 9 && c == 9) break;
				batch.add(new Position(r, c));
				if (batch.size() == 3) {
					game.fireShots(new java.util.ArrayList<>(batch));
					batch.clear();
				}
			}
		}
		String json = game.randomEnemyFire();
		assertNotNull(json, "Error: Should execute else branch when candidateShots < NUMBER_SHOTS.");
	}

	@Test
	@DisplayName("Test readEnemyFire - Valid Input")
	void readEnemyFire1() {
		Scanner scanner = new Scanner("A1 A2 A3");
		String json = game.readEnemyFire(scanner);
		assertNotNull(json, "Error: readEnemyFire should return serialized shots.");
		assertEquals(1, game.getAlienMoves().size(), "Error: Alien moves size should be 1.");
	}

	@Test
	@DisplayName("Test readEnemyFire - Letter without integer branch")
	void readEnemyFire2() {
		Scanner scanner = new Scanner("A B C");
		assertThrows(IllegalArgumentException.class, () -> {
			game.readEnemyFire(scanner);
		}, "Error: Should throw exception when letter is not followed by integer.");
	}

	@Test
	@DisplayName("Test readEnemyFire - All exception and token branches")
	void readEnemyFire3() {
		assertThrows(IllegalArgumentException.class, () -> {
			game.readEnemyFire(new Scanner("A1 A2 B"));
		}, "Error: Should throw exception for incomplete token at the end.");

		assertThrows(IllegalArgumentException.class, () -> {
			game.readEnemyFire(new Scanner("A1 A2"));
		}, "Error: Should throw exception when fewer than 3 shots are given.");

		assertDoesNotThrow(() -> {
			game.readEnemyFire(new Scanner("A 1 B 2 C 3"));
		}, "Error: Should parse separated letter and integer tokens.");
	}

	@Test
	@DisplayName("Test printBoardShots - Adjacent marker condition branch")
	void printBoardShots1() {
		Ship ship = new Barge(Compass.NORTH, new Position(1, 1));
		game.getMyFleet().addShip(ship);
		ship.getPositions().get(0).shoot();

		List<IPosition> shots = List.of(new Position(1, 2), new Position(0, 0), new Position(0, 1));
		game.fireShots(shots);

		assertDoesNotThrow(() -> game.printMyBoard(true, false), "Error: Should handle adjacent markers.");
	}

	@Test
	@DisplayName("Test printBoardShots - All branch combinations (outside, hit, adjacent, repeat)")
	void printBoardShots2() {
		Ship sunkBarge = new Barge(Compass.NORTH, new Position(0, 0));
		Ship floatingFrigate = new Frigate(Compass.EAST, new Position(5, 5));
		game.getMyFleet().addShip(sunkBarge);
		game.getMyFleet().addShip(floatingFrigate);
		sunkBarge.getPositions().get(0).shoot();

		game.fireShots(List.of(new Position(-1, -1), new Position(0, 1), new Position(5, 5)));

		game.fireShots(List.of(new Position(5, 5), new Position(9, 9), new Position(9, 8)));

		assertDoesNotThrow(() -> game.printMyBoard(true, true), "Error: Should cover all branches in printBoardShots.");
	}

	@Test
	@DisplayName("Test jsonShots serialization")
	void jsonShots1() {
		List<IPosition> shots = List.of(new Position(1, 1), new Position(2, 2));
		String json = Game.jsonShots(shots);
		assertNotNull(json, "Error: JSON string should not be null.");
		assertTrue(json.contains("row"), "Error: JSON should contain row.");
	}

	@Test
	@DisplayName("Test Getters for Fleets and Moves")
	void gettersAndBoards1() {
		assertNotNull(game.getMyFleet(), "Error: myFleet should not be null.");
		assertNotNull(game.getAlienFleet(), "Error: alienFleet should not be null.");
		assertNotNull(game.getMyMoves(), "Error: myMoves should not be null.");
		assertDoesNotThrow(() -> game.printMyBoard(false, false), "Error: printMyBoard should execute.");
		assertDoesNotThrow(() -> game.printAlienBoard(false, false), "Error: printAlienBoard should execute.");
		assertDoesNotThrow(() -> game.over(), "Error: over should execute.");
	}
}