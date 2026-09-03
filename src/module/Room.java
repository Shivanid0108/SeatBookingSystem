package module;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Room represents a hall or zone inside a Venue. Examples: Hall 3 in a cinema,
 * VIP section in a restaurant, Conference Room A.
 * 
 * WHY does Room exist separately from Venue? One Venue HAS-MANY Rooms. Each
 * Room has its own size and generates its own seats.
 * 
 * KEY RESPONSIBILITY OF ROOM: Room automatically generates ALL seats when
 * created. This is encapsulation — Room knows best how to create its own seats.
 * No one outside Room needs to know HOW seats are created.
 * 
 * RELATIONSHIP: Venue HAS-MANY Rooms (Room belongs to one Venue) Room HAS-MANY
 * Seats (auto-generated in constructor)
 * 
 * SPRING BOOT NOTE: - @Entity - @ManyToOne on venue field (many rooms belong to
 * one venue) - @OneToMany on seats (one room has many seats)
 */
public class Room {

	/**
	 * WHY Long id? Same reason as Venue — Spring Boot JPA standard. DB
	 * auto-generates this value.
	 */
	private Long id;

	/**
	 * WHY store roomSize? 1. Needed for displaySeatMap() to know grid dimensions 2.
	 * Needed for zone calculation (last row = row == roomSize) 3. Will be stored in
	 * DB so BusinessOwner's room size is persisted
	 * 
	 * Room is always SQUARE — size 5 means 5 rows × 5 columns = 25 seats. This is
	 * per the assignment specification.
	 */
	private int roomSize;

	/**
	 * WHY keep regularPrice in Room even though Seat uses it? 1. Stored in DB —
	 * when we load a Room from DB, we need the price 2. BusinessOwner might want to
	 * UPDATE the price later 3. Future feature: regenerate seats with new pricing
	 * 
	 * We don't want to lose this information after construction.
	 */
	private BigDecimal regularPrice;

	/**
	 * WHY List<Seat> and not Seat[]?
	 * 
	 * For console app: either works since size is fixed. For Spring Boot
	 * JPA: @OneToMany requires a Collection (List, Set). Arrays are NOT supported
	 * by JPA for relationships.
	 * 
	 * So List<Seat> is the right choice for Spring Boot compatibility.
	 */
	private List<Seat> seats = new ArrayList<>();

	/**
	 * WHY does constructor auto-generate seats?
	 * 
	 * Encapsulation — Room knows its own size, so Room creates its own seats. The
	 * caller (VenueService) just says "give me a room of size 5" and Room handles
	 * everything internally.
	 * 
	 * Alternative would be VenueService creating each seat manually — messy!
	 * 
	 * WHY pass regularPrice to constructor? Room needs it to pass to each Seat for
	 * zone price calculation. We don't import Venue to avoid circular dependency:
	 * Venue → Room → Seat (one direction only, clean!)
	 */
	public Room(int roomSize, BigDecimal regularPrice) {
		this.roomSize = roomSize;
		this.regularPrice = regularPrice;

		/**
		 * WHY start from 1 and not 0? Real world rows and columns start from 1, not 0.
		 * Seat "1A" is the first seat — "0A" makes no sense to users.
		 * 
		 * WHY nested loop? We need every combination of row and column. For size 5:
		 * rows 1-5 × cols 1-5 = 25 seats total.
		 */
		for (int row = 1; row <= roomSize; row++) {
			for (int col = 1; col <= roomSize; col++) {
				Seat seat = new Seat(row, col, roomSize, regularPrice);
				seats.add(seat);
			}
		}
	}

	// Getter only — id never changes
	public Long getId() {
		return id;
	}

	public int getRoomSize() {
		return roomSize;
	}

	public BigDecimal getRegularPrice() {
		return regularPrice;
	}

	public List<Seat> getSeats() {
		return seats;
	}

	/**
	 * WHY does displaySeatMap take showDateTime as parameter?
	 * 
	 * A seat's availability depends on WHEN the show is. Seat 3C might be: -
	 * Available for March 20 7pm show ✅ - Booked for March 21 7pm show ❌
	 * 
	 * Without showDateTime, we can't know which slots are booked. The caller passes
	 * the show time they want to check.
	 * 
	 * WHY is this in Room and not in a Service class? displaySeatMap is about
	 * displaying Room's own data — it belongs here. Service classes handle business
	 * logic, not display logic for a single Room.
	 */
	public void displaySeatMap(LocalDateTime showDateTime) {

		// Print column headers (A, B, C...)
		// Extra spaces at start to align with row numbers
		System.out.print("      ");
		for (int col = 1; col <= roomSize; col++) {
			char colLetter = (char) ('A' + col - 1);
			System.out.printf("  [%c] ", colLetter);
		}
		System.out.println();

		for (Seat seat : seats) {

			/**
			 * WHY check if column == 'A'? When column resets to 'A', a new row is starting.
			 * That's when we: 1. Print a newline to end the previous row 2. Print the row
			 * number for the new row
			 * 
			 * %2d formats the row number to always be 2 characters wide. This keeps
			 * alignment consistent for rows 1-99.
			 */
			if (seat.getColumn() == 'A') {
				System.out.println();
				System.out.printf("%2d    ", seat.getRow());
			}

			/**
			 * WHY check isAvailable BEFORE zone? If we checked zone first, we'd never reach
			 * the [XX] condition because every seat has a zone (1, 2, 3, or 4).
			 * 
			 * Check availability first — if booked show [XX], otherwise show zone symbol.
			 */
			if (!seat.isAvailableFor(showDateTime)) {
				System.out.printf("%-6s", "[XX]");
			} else if (seat.getZone() == 1) {
				System.out.printf("%-6s", "[Z1]");
			} else if (seat.getZone() == 2) {
				System.out.printf("%-6s", "[Z2]");
			} else if (seat.getZone() == 3) {
				System.out.printf("%-6s", "[Z3]");
			} else {
				System.out.printf("%-6s", "[Z4]");
			}
		}
		System.out.println();
		System.out.println();

		// Print zone legend so user knows what each symbol means
		System.out.println("Zone Legend:");
		System.out.println("[Z1] First row     - Price x2.00 (most expensive)");
		System.out.println("[Z2] Last row      - Price x0.75 (cheapest)");
		System.out.println("[Z3] Middle cols   - Price x1.25");
		System.out.println("[Z4] Regular seats - Regular price");
		System.out.println("[XX] Booked        - Not available for this show");
	}
}