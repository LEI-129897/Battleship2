package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link Position}.
 * Author: luiberna
 * Date: 2026-11-18 03:19 UTC
 * Cyclomatic Complexity (McCabe/Sonar-style):
 * - randomPosition(): 1
 * - Position(char, int): 1
 * - Position(int, int): 1
 * - getRow(): 1
 * - getColumn(): 1
 * - getClassicRow(): 1
 * - getClassicColumn(): 1
 * - isInside(): 4
 * - isAdjacentTo(): 2
 * - adjacentPositions(): 3
 * - isOccupied(): 1
 * - isHit(): 1
 * - occupy(): 1
 * - shoot(): 1
 * - equals(): 4
 * - hashCode(): 1
 * - toString(): 1
 */
class PositionTest {
	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	@Test
	@DisplayName("randomPosition returns coordinates inside the board")
	void randomPosition() {
		Position random = Position.randomPosition();

		assertAll(
				() -> assertTrue(random.isInside(), "Error: expected a random position inside the board"),
				() -> assertTrue(random.getRow() >= 0, "Error: expected a non-negative random row"),
				() -> assertTrue(random.getRow() < Game.BOARD_SIZE, "Error: expected the random row below board size"),
				() -> assertTrue(random.getColumn() >= 0, "Error: expected a non-negative random column"),
				() -> assertTrue(random.getColumn() < Game.BOARD_SIZE,
						"Error: expected the random column below board size"));
	}

	@Test
	@DisplayName("Character constructor converts a lowercase row and one-based column")
	void constructor1() {
		Position created = new Position('c', 4);

		assertAll(
				() -> assertEquals(2, created.getRow(), "Error: expected lowercase c to map to row 2"),
				() -> assertEquals(3, created.getColumn(), "Error: expected column 4 to map to index 3"),
				() -> assertFalse(created.isOccupied(), "Error: expected a new position to be unoccupied"),
				() -> assertFalse(created.isHit(), "Error: expected a new position to be unhit"));
	}

	@Test
	@DisplayName("Integer constructor stores zero-based coordinates and initializes flags")
	void constructor2() {
		Position created = new Position(2, 3);

		assertAll(
				() -> assertEquals(2, created.getRow(), "Error: expected row 2"),
				() -> assertEquals(3, created.getColumn(), "Error: expected column 3"),
				() -> assertFalse(created.isOccupied(), "Error: expected a new position to be unoccupied"),
				() -> assertFalse(created.isHit(), "Error: expected a new position to be unhit"));
	}

	@Test
	@DisplayName("getRow returns the zero-based row")
	void getRow() {
		assertEquals(2, position.getRow(), "Error: expected row 2");
	}

	@Test
	@DisplayName("getColumn returns the zero-based column")
	void getColumn() {
		assertEquals(3, position.getColumn(), "Error: expected column 3");
	}

	@Test
	@DisplayName("getClassicRow returns the uppercase row letter")
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "Error: expected row letter C");
	}

	@Test
	@DisplayName("getClassicColumn returns the one-based column")
	void getClassicColumn() {
		assertEquals(4, position.getClassicColumn(), "Error: expected classic column 4");
	}

	@Test
	@DisplayName("isInside1 accepts an inside position and rejects a negative row")
	void isInside1() {
		assertAll(
				() -> assertTrue(position.isInside(), "Error: expected (2,3) to be inside the board"),
				() -> assertFalse(new Position(-1, 3).isInside(),
						"Error: expected a negative row to be outside the board"));
	}

	@Test
	@DisplayName("isInside2 rejects a negative column")
	void isInside2() {
		assertFalse(new Position(2, -1).isInside(), "Error: expected a negative column to be outside the board");
	}

	@Test
	@DisplayName("isInside3 rejects a row at the board limit")
	void isInside3() {
		assertFalse(new Position(Game.BOARD_SIZE, 3).isInside(),
				"Error: expected a row equal to board size to be outside the board");
	}

	@Test
	@DisplayName("isInside4 rejects a column at the board limit")
	void isInside4() {
		assertFalse(new Position(2, Game.BOARD_SIZE).isInside(),
				"Error: expected a column equal to board size to be outside the board");
	}

	@Test
	@DisplayName("isAdjacentTo1 rejects a row more than one away")
	void isAdjacentTo1() {
		assertFalse(position.isAdjacentTo(new Position(4, 3)),
				"Error: expected a row distance greater than one to be non-adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo2 checks the column condition and rejects null")
	void isAdjacentTo2() {
		assertAll(
				() -> assertTrue(position.isAdjacentTo(new Position(3, 4)),
						"Error: expected a diagonal neighbor to be adjacent"),
				() -> assertFalse(position.isAdjacentTo(new Position(3, 5)),
						"Error: expected a column distance greater than one to be non-adjacent"),
				() -> assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
						"Error: expected null input to throw NullPointerException"));
	}

	@Test
	@DisplayName("adjacentPositions1 returns all eight neighbors for an interior position")
	void adjacentPositions1() {
		assertEquals(List.of(
						new Position(1, 3), new Position(2, 4), new Position(3, 3), new Position(2, 2),
						new Position(3, 4), new Position(3, 2), new Position(1, 4), new Position(1, 2)),
				position.adjacentPositions(), "Error: expected all eight valid neighbors in direction order");
	}

	@Test
	@DisplayName("adjacentPositions2 excludes out-of-board neighbors at a corner")
	void adjacentPositions2() {
		assertEquals(List.of(new Position(0, 1), new Position(1, 0), new Position(1, 1)),
				new Position(0, 0).adjacentPositions(),
				"Error: expected only the three in-board neighbors at the top-left corner");
	}

	@Test
	@DisplayName("adjacentPositions3 returns five neighbors along an edge")
	void adjacentPositions3() {
		assertEquals(List.of(
						new Position(0, 6), new Position(1, 5), new Position(0, 4), new Position(1, 6),
						new Position(1, 4)),
				new Position(0, 5).adjacentPositions(),
				"Error: expected five in-board neighbors along the top edge");
	}

	@Test
	@DisplayName("isOccupied reports the state before and after occupying")
	void isOccupied() {
		assertAll(
				() -> assertFalse(position.isOccupied(), "Error: expected a new position to be unoccupied"),
				() -> {
					position.occupy();
					assertTrue(position.isOccupied(), "Error: expected occupy() to mark the position occupied");
				});
	}

	@Test
	@DisplayName("isHit reports the state before and after shooting")
	void isHit() {
		assertAll(
				() -> assertFalse(position.isHit(), "Error: expected a new position to be unhit"),
				() -> {
					position.shoot();
					assertTrue(position.isHit(), "Error: expected shoot() to mark the position hit");
				});
	}

	@Test
	@DisplayName("occupy marks the position occupied")
	void occupy() {
		position.occupy();

		assertTrue(position.isOccupied(), "Error: expected occupy() to mark the position occupied");
	}

	@Test
	@DisplayName("shoot marks the position hit")
	void shoot() {
		position.shoot();

		assertTrue(position.isHit(), "Error: expected shoot() to mark the position hit");
	}

	@Test
	@DisplayName("equals1 handles identity, null, and unrelated objects")
	void equals1() {
		assertAll(
				() -> assertTrue(position.equals(position), "Error: expected a position to equal itself"),
				() -> assertFalse(position.equals(null), "Error: expected a position not to equal null"),
				() -> assertFalse(position.equals(new Object()),
						"Error: expected a position not to equal an unrelated object"));
	}

	@Test
	@DisplayName("equals2 accepts an IPosition with matching coordinates")
	void equals2() {
		assertTrue(position.equals(new Position(2, 3)),
				"Error: expected positions with matching coordinates to be equal");
	}

	@Test
	@DisplayName("equals3 rejects a different row")
	void equals3() {
		assertFalse(position.equals(new Position(1, 3)),
				"Error: expected positions with different rows not to be equal");
	}

	@Test
	@DisplayName("equals4 rejects a different column")
	void equals4() {
		assertFalse(position.equals(new Position(2, 4)),
				"Error: expected positions with different columns not to be equal");
	}

	@Test
	@DisplayName("hashCode remains consistent for equal positions after state changes")
	void hashCodeTest() {
		Position sameCoordinates = new Position(2, 3);
		sameCoordinates.occupy();
		sameCoordinates.shoot();

		assertAll(
				() -> assertEquals(position, sameCoordinates,
						"Error: expected mutable flags not to affect coordinate equality"),
				() -> assertEquals(position.hashCode(), sameCoordinates.hashCode(),
						"Error: expected equal positions to have matching hash codes"));
	}

	@Test
	@DisplayName("toString returns the classic row and column")
	void toStringTest() {
		assertEquals("C4", position.toString(), "Error: expected classic coordinate C4");
	}
}
