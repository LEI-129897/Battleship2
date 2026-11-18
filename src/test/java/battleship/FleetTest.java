package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Test class for Fleet.
 * Author: ${user.name}
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - addShip: 3
 * - getShips: 1
 * - getShipsLike: 2
 * - getFloatingShips: 2
 * - shipAt: 2
 * - isInsideBoard: 3
 * - colisionRisk: 2
 */
	public class FleetTest {

		private Fleet fleet;

		@BeforeEach
		void setUp() {
			fleet = new Fleet();
		}

		@AfterEach
		void tearDown() {
			fleet = null;
		}

		/**
		 * Test for the Fleet constructor.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testConstructor() {
			assertNotNull(fleet, "Error: Instance of Fleet should not be null.");
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet should be initialized with empty ships list.");
		}

		@Test
		void testCreateRandom(){
				IFleet r = Fleet.createRandom();
				assertNotNull(r, "Error: createRandom should return a non-null fleet instance.");
				assertEquals(Fleet.FLEET_SIZE, r.getShips().size(), "Error: createRandom should create a fleet with the correct number of ships.");
		}

		/**
		 * Test for the addShip method (all conditions true).
		 * Cyclomatic Complexity: 3
		 */
		@Test
		void testAddShip1() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			assertTrue(fleet.addShip(ship), "Error: Valid ship should be added successfully.");
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should contain one ship after addition.");
		}

		/**
		 * Test for the addShip method (fleet size limit reached).
		 */
		@Test
		void testAddShip2() {
			for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
				fleet.addShip(new Barge(Compass.NORTH, new Position(i, 0)));
			}
			IShip anotherShip = new Barge(Compass.NORTH, new Position(10, 10));
			assertFalse(fleet.addShip(anotherShip), "Error: Should not add ship when fleet size limit is reached.");
		}

		/**
		 * Test for the addShip method (ship outside the board).
		 */
		@Test
		void testAddShip3() {
			IShip shipOutside = new Barge(Compass.NORTH, new Position(99, 99));
			assertFalse(fleet.addShip(shipOutside), "Error: Should not add ship outside the board.");
		}

		/**
		 * Test for the addShip method (collision risk).
		 */
		@Test
		void testAddShip4() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);
			assertFalse(fleet.addShip(ship2), "Error: Should not add ship with a collision risk.");
		}

		/**
		 * Test for the getShips method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testGetShips() {
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet's ships list should initially be empty.");
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should have size 1 after adding a ship.");
			assertEquals(ship, fleet.getShips().get(0), "Error: Fleet's first ship should match the added ship.");
		}

		/**
		 * Test for the getShipsLike method (ships of specific category).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testGetShipsLike() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(2, 1));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> barges = fleet.getShipsLike("Barca");
			assertEquals(1, barges.size(), "Error: There should be exactly one ship of category 'Barca'.");
			assertEquals(ship1, barges.get(0), "Error: The ship of category 'Barca' does not match.");
		}

		/**
		 * Test for the getFloatingShips method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testGetFloatingShips() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> floatingShips = fleet.getFloatingShips();
			assertEquals(2, floatingShips.size(), "Error: All ships should be floating initially.");

			ship1.getPositions().get(0).shoot();  // Sink ship1
			floatingShips = fleet.getFloatingShips();
			assertEquals(1, floatingShips.size(), "Error: Only one ship should be floating after sinking one.");
			assertEquals(ship2, floatingShips.get(0), "Error: The floating ship should match the expected result.");
		}

		/**
		 * Test for the shipAt method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testShipAt() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			assertEquals(ship, fleet.shipAt(new Position(1, 1)), "Error: Should return the correct ship at the position.");
			assertNull(fleet.shipAt(new Position(5, 5)), "Error: Should return null for empty positions in the fleet.");
		}


		/**
		 * Test for private method colisionRisk.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testColisionRisk() throws Exception {
			var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
			method.setAccessible(true);

			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);

			assertTrue((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should be at collision risk.");
			assertFalse((Boolean) method.invoke(fleet, new Barge(Compass.NORTH, new Position(5, 5))),
					"Error: Ships at non-overlapping positions should not have a collision risk.");
		}

		/**
		 * Test for the printStatus method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testPrintStatus() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertDoesNotThrow(fleet::printStatus, "Error: printStatus should not throw any exceptions.");
		}

		/**
		 * Test for getSunkShips (Path 1: initially empty / branch false).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("Test getSunkShips - Initially Empty")
		void testGetSunkShips1() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertTrue(fleet.getSunkShips().isEmpty(), "Error: Sunk ships should be empty initially.");
		}

		/**
		 * Test for getSunkShips (Path 2: with sunk ships / branch true).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("Test getSunkShips - With Sunk Ships")
		void testGetSunkShips2() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			ship.getPositions().get(0).shoot();

			List<IShip> sunk = fleet.getSunkShips();
			assertAll("Sunk ships checks",
					() -> assertEquals(1, sunk.size(), "Error: Should have one sunk ship."),
					() -> assertEquals(ship, sunk.get(0), "Error: Sunk ship instance should match.")
			);
		}

		/**
		 * Test for getShipsLike (Path 2: category not found / branch false).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("Test getShipsLike - Category Not Found")
		void testGetShipsLike2() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship1);

			List<IShip> results = fleet.getShipsLike("Galeao");
			assertTrue(results.isEmpty(), "Error: Should return empty list for non-existent category.");
		}

		/**
		 * Test for printAllShips.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		@DisplayName("Test printAllShips")
		void testPrintAllShips() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertDoesNotThrow(() -> fleet.printAllShips(), "Error: printAllShips should not throw exceptions.");
		}

		/**
		 * Test for printFloatingShips.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		@DisplayName("Test printFloatingShips")
		void testPrintFloatingShips() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertDoesNotThrow(() -> fleet.printFloatingShips(), "Error: printFloatingShips should not throw exceptions.");
		}

		/**
		 * Test for printShipsByCategory (Path 1: valid category).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("Test printShipsByCategory - Valid")
		void testPrintShipsByCategory1() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertDoesNotThrow(() -> fleet.printShipsByCategory("Barca"), "Error: printShipsByCategory should not throw exceptions.");
		}

		/**
		 * Test for printShipsByCategory (Path 2: non-existent category).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		@DisplayName("Test printShipsByCategory - Non-existent")
		void testPrintShipsByCategory2() {
			assertDoesNotThrow(() -> fleet.printShipsByCategory("Inexistente"), "Error: printShipsByCategory should handle missing category safely.");
		}
	}