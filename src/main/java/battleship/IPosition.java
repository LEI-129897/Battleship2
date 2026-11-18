package battleship;

import java.util.List;

/**
 * The interface Position.
 *
 * @author fba
 */
public interface IPosition
{
	/**
	 * Gets row.
	 *
	 * @return the row
	 */
	int getRow();

	/**
	 * Gets column.
	 *
	 * @return the column
	 */
	int getColumn();

	/**
	 * Gets traditional row.
	 *
	 * @return the traditional row within [A-J]
	 */
	char getClassicRow();

	/**
	 * Gets traditional column.
	 *
	 * @return the traditional column within [1-10]
	 */
	int getClassicColumn();

	/**
	 * Equals boolean.
	 *
	 * @param other the other
	 * @return the boolean
	 */
	boolean equals(Object other);

	/**
	 * Checks if the position is inside the board.
	 *
	 * @return true if the position is inside the board, false otherwise
	 */
	boolean isInside();

	/**
	 * Is adjacent to boolean.
	 *
	 * @param other the other
	 * @return the boolean
	 */
	boolean isAdjacentTo(IPosition other);

	/**
	 * Occupy.
	 */
	void occupy();

	/**
	 * Shoot.
	 */
	void shoot();

	/**
	 * Is occupied boolean.
	 *
	 * @return the boolean
	 */
	boolean isOccupied();

	/**
	 * Is hit boolean.
	 *
	 * @return the boolean
	 */
	boolean isHit();

	/**
	 * Retrieves a list of positions that are adjacent to the current position.
	 * Adjacent positions are defined as those sharing a border or corner
	 * with the current position.
	 *
	 * @return a list of adjacent positions as IPosition objects
	 */
	List<IPosition> adjacentPositions();

	/**
	 * Fills the positions of the Galleon when oriented to the NORTH.
	 *
	 * @param galleon
	 */
	default void fillNorth(Galleon galleon) {
		for (int i = 0; i < 3; i++) {
			galleon.getPositions().add(new Position(getRow(), getColumn() + i));
		}
		galleon.getPositions().add(new Position(getRow() + 1, getColumn() + 1));
		galleon.getPositions().add(new Position(getRow() + 2, getColumn() + 1));
	}
}
