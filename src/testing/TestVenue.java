package testing;

import java.math.BigDecimal;

import module.Room;
import module.Venue;

public class TestVenue {
	public static void main(String[] args) {
		Venue venue = new Venue("Cineplex", "Cinema", new BigDecimal("100"));
		Room room = new Room(7, new BigDecimal("100"));
		venue.addRoom(room);
		room.displaySeatMap();
	}
}