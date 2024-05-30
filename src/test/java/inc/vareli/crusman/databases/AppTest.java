package inc.vareli.crusman.databases;

import inc.vareli.crusman.databases.Ship.RoomType;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.util.EnumMap;

public class AppTest {
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

	@Test
	public void shipTest() {
		EnumMap<RoomType,Integer> roomCounts = new EnumMap<RoomType,Integer>(RoomType.class);
		roomCounts.put(RoomType.INTERIOR, 1);
		roomCounts.put(RoomType.OUTSIDE, 1);
		roomCounts.put(RoomType.BALCONY, 1);
		roomCounts.put(RoomType.SUITE, 2);
		Ship s = new Ship(0, roomCounts);

	// 	assertTrue(s.addPerson(RoomType.INTERIOR, 2));
	// 	assertTrue(s.addPerson(RoomType.INTERIOR, 2));

	// 	//checking for capacity
	// 	assertTrue(!s.addPerson(RoomType.INTERIOR, 2));

	// 	//showing that they don't have memory
	// 	assertTrue(s.addPerson(RoomType.INTERIOR, 3));
		
	// 	//seeing if it works for the other type
	// 	assertTrue(s.addPerson(RoomType.OUTSIDE, 1));
	// 	assertTrue(!s.addPerson(RoomType.OUTSIDE, 1));

	// 	//and if it works out of order
	// 	assertTrue(s.addPerson(RoomType.BALCONY, 2));
	// 	assertTrue(s.addPerson(RoomType.SUITE, 1));
	// 	//seeing if multiple rooms works
	// 	assertTrue(s.addPerson(RoomType.SUITE, 1));
	// 	assertTrue(!s.addPerson(RoomType.SUITE, 1));
	// 	assertTrue(s.getTotalOccupancy(RoomType.INTERIOR) == 3);
	// 	assertTrue(s.getTotalOccupancy(RoomType.OUTSIDE) == 1);
	 }
}
