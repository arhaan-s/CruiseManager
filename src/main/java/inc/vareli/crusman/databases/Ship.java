package inc.vareli.crusman.databases;

import java.util.Map;
import java.util.Iterator;

public class Ship {

	protected final long ID;
	protected Room[] rooms;

	/**
	 * A constructor. Creates a Ship with the specified ID and room counts
	 * Protected so that user can ONLY create a Ship through CMConnection, 
	 * ensuring that everything is written to DB
	 * @param ID - a unique identifier
	 * @param roomCounts - a Map from RoomType to Integer, specifies how many
	 * of each type of room there is
	 */
	protected Ship(long ID, Map<RoomType,Integer> roomCounts) {
		int size = roomCounts.values().stream().mapToInt(t->t).sum();
		this.ID = ID;
		this.rooms = new Room[size];

		Iterator<RoomType> iter = roomCounts.keySet().iterator();
		int prev = 0;
		do {
			RoomType currentType = iter.next();
			int i;
			for (i = prev; i <  prev + roomCounts.get(currentType); i++) {
				rooms[i] = new Room(currentType);
			}
			prev = i;
		} while (iter.hasNext());
	}

	/**
	 * Attempts to add a person to a room of the specified type, given that we
	 * can allow up to maxOccupancy people per room
	 * @param roomType - the type of room that we want
	 * @param maxOccupancy - the maximum people we can have in a room
	 * @return if this operation succeeded or not
	 */
	public int addPerson(RoomType roomType, int maxOccupancy) {
		for (int i = 0; i < rooms.length; i++) {
			if (rooms[i] != null && rooms[i].type == roomType && rooms[i].count < maxOccupancy) {
				rooms[i].count++;
				return (roomType.ordinal()*100)+i;
			}
		}
		return -1;
	}

	/**
	 * A method to get the total occupancy of all the rooms of roomType on
	 * this Ship
	 * @param roomType - the type of the room we want to find the total occupancy of
	 * @return the total occupancy of all rooms of type roomType
	 */
	public int getTotalOccupancy(RoomType roomType) {
		int sum = 0;
		for (Room r : rooms) {
			if (r.type == roomType) {
				sum += r.count;
			}
		}
		return sum;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (RoomType type : RoomType.values()) {
			int typeCount = 0;
			for (Room r : rooms) {
				if (r.type == type) {
					typeCount++;
				}
			}
			builder.append(typeCount + " " + type + " rooms.\n");
		}
		return builder.toString();
	}

	private static class Room {
		public RoomType type;
		public int count;

		public Room(RoomType type) {
			this.type = type;
			count = 0;
		}
	}

	/**
	 * An enum of the possible types of rooms. Rooms are a type of cost as well.
	 */
	public enum RoomType implements CostType {
		INTERIOR("Interior"),
		OUTSIDE("Outside"),
		BALCONY("Balcony"),
		SUITE("Suite");

		private String name;

		private RoomType(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}
}
