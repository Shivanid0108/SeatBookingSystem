package module;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {

	private String roomID;
	private int roomSize;
	private BigDecimal regularPrice;
	private List<Seat> seats = new ArrayList<>();

	public Room(int roomSize, BigDecimal regularPrice) {
		this.roomSize = roomSize;
		this.regularPrice = regularPrice;
		this.roomID = UUID.randomUUID().toString();
		for (int row = 1; row <= roomSize; row++) {
			for (int col = 1; col <= roomSize; col++) {
				Seat seat = new Seat(row, col, roomSize, regularPrice);
				seats.add(seat);
			}
		}
	}

	public String getRoomID() {
		return roomID;
	}

	public int getRoomSize() {
		return roomSize;
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void displaySeatMap() {
		System.out.println("     ");
		for (int col = 1; col <= roomSize; col++) {
			char colLetter = (char) ('A' + col - 1);
			System.out.print("   [" + colLetter + "]  ");
		}
		System.out.println();

		for (Seat seat : seats) {
			if (seat.getColumn() == 'A') {
				System.out.println();
				System.out.printf("%2d   ", seat.getRow());
			}

			if (!seat.isAvailable()) {
				System.out.printf("%-6s", "[XX]");
			} else if (seat.getZone() == 1) {
				System.out.printf("%-7s", "[Z1] ");
			} else if (seat.getZone() == 2) {
				System.out.printf("%-6s", "[Z2] ");
			} else if (seat.getZone() == 3) {
				System.out.printf("%-6s", "[Z3] ");
			} else {
				System.out.printf("%-6s", "[Z4] ");
			}
		}
		System.out.println();
	}

}
