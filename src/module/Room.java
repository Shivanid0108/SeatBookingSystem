package module;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * WHAT: Represents a hall or screen inside a Venue. Examples: Screen 3 in a
 * cinema, VIP section in a restaurant, Hall A in a conference centre.
 *
 * KEY RESPONSIBILITY: Room automatically generates ALL seats when created. This
 * is encapsulation — Room knows its size and price, so Room creates its own
 * seats. No external class needs to know HOW seats are created.
 *
 * WHY SEPARATE ROWS AND COLUMNS? Original design used roomSize (square only,
 * max 26x26). Real venues are NOT square — a sports arena might be 200 rows x
 * 26 cols. Separating totalRows and totalCols removes the 26-row limit. Max
 * cols still 26 (A-Z alphabet limit for column labels). Max rows 200
 * (reasonable real-world maximum).
 *
 * WHY 2D ARRAY instead of List<Seat>? - seatGrid[row][col] gives O(1) direct
 * access to any seat - Natural grid representation matches the physical room
 * layout - Makes adjacent seat finding easy: seatGrid[r][c] and
 * seatGrid[r][c+1] are adjacent - Sliding window algorithm works directly on
 * the 2D structure
 *
 * TRADE-OFF for Spring Boot: JPA @OneToMany requires a Collection (List, Set) —
 * not arrays. When migrating to Spring Boot, seatGrid will become List<Seat>
 * with row/col stored as fields in Seat for DB mapping.
 *
 * RELATIONSHIPS: - Venue HAS-MANY Rooms - Room HAS-MANY Seats (auto-generated
 * in constructor) - Room HAS-MANY Shows (scheduled by BusinessOwner)
 */
public class Room {

	private Long id;

	/**
	 * WHY store totalRows and totalCols separately? 1. Needed for displaySeatMap()
	 * to know grid dimensions 2. Needed for zone calculation (last row = row ==
	 * totalRows) 3. Stored in DB so BusinessOwner's room configuration is persisted
	 * 4. Allows non-square rooms (50 rows x 10 cols for a lecture hall)
	 */
	private int totalRows;
	private int totalCols;

	/**
	 * WHY keep regularPrice in Room? 1. Stored in DB — when we load a Room from DB,
	 * we need the price 2. BusinessOwner might update the price later 3. Needed if
	 * seats are ever regenerated with new pricing We don't want to lose this after
	 * construction.
	 */
	private BigDecimal regularPrice;

	/**
	 * WHY Seat[][] instead of List<Seat>? 2D array naturally represents the
	 * physical room grid. seatGrid[row][col] = O(1) direct access to any seat.
	 * Adjacent seats: seatGrid[r][c] and seatGrid[r][c+1]. Trade-off: JPA doesn't
	 * support arrays for @OneToMany → List in Spring Boot.
	 */
	private Seat[][] seatGrid;

	/**
	 * WHY List<Show>? BusinessOwner schedules multiple shows in one room. One room
	 * can have 4 shows a day (10am, 2pm, 6pm, 9pm). Each is a separate Show object
	 * stored here.
	 */
	private List<Show> shows = new ArrayList<>();

	/**
	 * WHY does constructor auto-generate seats? Encapsulation — Room knows its size
	 * and price, so Room creates seats. VenueService just says "give me a room of
	 * size 10x20" and Room handles everything internally.
	 *
	 * WHY start from 1 and not 0? Real world rows/cols start from 1, not 0. Seat
	 * "1A" is the first seat — "0A" makes no sense to users.
	 *
	 * WHY seatGrid[row-1][col-1]? Our loop goes 1 to totalRows but arrays are
	 * 0-indexed. row=1, col=1 → seatGrid[0][0] (first position) row=5, col=5 →
	 * seatGrid[4][4] (for a 5x5 room)
	 */
	public Room(int totalRows, int totalCols, BigDecimal regularPrice) {
		this.totalRows = totalRows;
		this.totalCols = totalCols;
		this.regularPrice = regularPrice;
		this.seatGrid = new Seat[totalRows][totalCols];

		for (int row = 1; row <= totalRows; row++)
			for (int col = 1; col <= totalCols; col++)
				seatGrid[row - 1][col - 1] = new Seat(row, col, totalRows, totalCols, regularPrice);
	}

	// ── Show management ────────────────────────────────────────────────────

	public void addShow(Show show) {
		shows.add(show);
	}

	/**
	 * WHY two separate getShow methods? getActiveShows() → what CLIENTS see (only
	 * bookable shows) getAllShows() → what BUSINESSOWNER sees (including cancelled
	 * shows) This separation ensures cancelled shows disappear from client view
	 * while remaining visible to the owner for record keeping.
	 */
	public List<Show> getActiveShows() {
		List<Show> active = new ArrayList<>();
		for (Show s : shows)
			if (s.isActive())
				active.add(s);
		return active;
	}

	public List<Show> getAllShows() {
		return shows;
	}

	// ── Seat map display ───────────────────────────────────────────────────

	/**
	 * WHY does displaySeatMap take showDateTime as parameter? A seat's availability
	 * depends on WHEN the show is. Seat 3C might be booked for the 7pm show but
	 * free for the 9pm show. Caller passes the show time — we check availability
	 * for that specific slot.
	 *
	 * WHY check isAvailable BEFORE zone? Every seat has a zone (1-4), so checking
	 * zone first means the [XX] condition would never be reached. Availability
	 * check must come first.
	 */
	public void displaySeatMap(LocalDateTime showDateTime) {
		System.out.print("\n       ");
		for (int col = 1; col <= totalCols; col++)
			System.out.printf(" [%c]", (char) ('A' + col - 1));
		System.out.println();

		for (int r = 0; r < totalRows; r++) {
			System.out.printf("%3d  ", r + 1);
			for (int c = 0; c < totalCols; c++) {
				Seat seat = seatGrid[r][c];
				if (!seat.isAvailableFor(showDateTime))
					System.out.print("[XX]");
				else
					System.out.printf("[Z%d]", seat.getZone());
			}
			System.out.println();
		}
		System.out.println("\nZ1=Front row(x2.00)  Z2=Back row(x0.75)  Z3=Middle cols(x1.25)  Z4=Regular  XX=Booked");
	}

	// ── Getters ────────────────────────────────────────────────────────────

	public Long getId() {
		return id;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public int getTotalCols() {
		return totalCols;
	}

	public BigDecimal getRegularPrice() {
		return regularPrice;
	}

	public Seat[][] getSeats() {
		return seatGrid;
	}
}