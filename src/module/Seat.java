package module;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * WHAT: Represents one individual seat inside a Room. Identified by a label
 * like "3C" (row 3, column C).
 *
 * KEY RESPONSIBILITIES: 1. Know its position (row, column) 2. Know its zone
 * (1-4) and calculated price 3. Track which time slots are booked (bookedSlots)
 * 4. Answer "am I available for this show time?"
 *
 * WHY SEAT CALCULATES ITS OWN ZONE AND PRICE? Encapsulation — Seat knows its
 * position (row, col, totalRows, totalCols). It has all the information needed
 * to determine its zone. No external class needs to do this calculation.
 *
 * ZONE PRICING (from assignment): Zone 1 (first row) → regularPrice × 2.00
 * (100% more expensive) Zone 2 (last row) → regularPrice × 0.75 (25% cheaper)
 * Zone 3 (middle cols) → regularPrice × 1.25 (25% more expensive) Zone 4
 * (remaining) → regularPrice × 1.00 (regular price)
 *
 * SPRING BOOT NOTE: - @Entity - @ManyToOne on room (many seats belong to one
 * room) - bookedSlots → @ElementCollection (separate junction table in DB)
 */
public class Seat {

	private Long id;

	/**
	 * WHY seatLabel ("3C") AND separate row/column fields? seatLabel → display to
	 * user ("Your seat is 3C") row → zone logic needs int: if(row == 1) → Zone 1
	 * column → display as letter ('C')
	 *
	 * We can't do math on "3C" as a String. row and column kept for internal logic;
	 * seatLabel for display.
	 */
	private String seatLabel;
	private int row;
	private char column;

	/**
	 * WHY BigDecimal for price? Money must NEVER use double or float. 0.1 + 0.2 =
	 * 0.30000000000000004 in double — catastrophic for billing! BigDecimal is
	 * exact. No rounding errors.
	 *
	 * calculatedPrice = regularPrice × zone multiplier. Computed once in
	 * constructor, never changes.
	 */
	private BigDecimal calculatedPrice;

	/**
	 * WHY List<LocalDateTime> bookedSlots instead of boolean isAvailable?
	 *
	 * OLD: boolean isAvailable Problem: a seat is available for SOME shows, not
	 * others. Seat 3C booked for 7pm show is still FREE for 9pm show. Boolean can't
	 * capture time-based availability.
	 *
	 * NEW: List<LocalDateTime> bookedSlots Stores every show time that has been
	 * booked for this seat. isAvailableFor(showDateTime) = is this time NOT in my
	 * bookedSlots?
	 *
	 * SPRING BOOT: @ElementCollection → separate seat_booked_slots table in DB.
	 */
	private List<LocalDateTime> bookedSlots = new ArrayList<>();

	/**
	 * WHY store zone as a field? Room.displaySeatMap() calls seat.getZone() for
	 * every seat. Without storing it, we'd recalculate zone on every display call.
	 * Store once in constructor, use many times.
	 */
	private int zone;

	/**
	 * Constructor — called by Room's nested loop for every seat position.
	 *
	 * WHY (char)('A' + colIndex - 1)? Java treats chars as numbers: 'A'=65, 'B'=66,
	 * 'C'=67... colIndex=1 → 65+1-1=65='A', colIndex=2 → 66='B', colIndex=3 →
	 * 67='C' Cast (char) converts the number back to a character.
	 *
	 * WHY row + "" + column for seatLabel? row is int, column is char. Adding ""
	 * forces Java to treat it as String concatenation. 3 + "" + 'C' = "3C"
	 *
	 * WHY separate even/odd roomSize logic for middle columns? Even roomSize (e.g.
	 * 10): two perfect middle cols exist: 5 and 6 roomSize/2 = 5, roomSize/2+1 = 6
	 * ✅ Odd roomSize (e.g. 5): integer division rounds down! roomSize/2 = 2 (wrong
	 * — picks cols 2 and 3, not 3 and 4) Fix: roomSize/2+1 = 3 and roomSize/2+2 = 4
	 * ✅
	 *
	 * WHY check Zone 1 and 2 BEFORE Zone 3? A seat in first row AND a middle column
	 * → Zone 1, not Zone 3. First/last row takes priority over middle column. Order
	 * of if-else determines priority.
	 *
	 * WHY multiply instead of add/subtract for pricing? Cleaner math: regularPrice
	 * + 100% = regularPrice × 2 (not price + price × 2!) regularPrice - 25% =
	 * regularPrice × 0.75
	 */
	public Seat(int row, int colIndex, int totalRows, int totalCols, BigDecimal regularPrice) {
		this.row = row;
		this.column = (char) ('A' + colIndex - 1);
		this.seatLabel = row + "" + column;

		boolean isMiddleCol = (totalCols % 2 == 0) ? (colIndex == totalCols / 2 || colIndex == totalCols / 2 + 1)
				: (colIndex == totalCols / 2 + 1 || colIndex == totalCols / 2 + 2);

		if (row == 1) {
			this.zone = 1;
			calculatedPrice = regularPrice.multiply(new BigDecimal("2"));
		} else if (row == totalRows) {
			this.zone = 2;
			calculatedPrice = regularPrice.multiply(new BigDecimal("0.75"));
		} else if (isMiddleCol) {
			this.zone = 3;
			calculatedPrice = regularPrice.multiply(new BigDecimal("1.25"));
		} else {
			this.zone = 4;
			calculatedPrice = regularPrice;
		}
	}

	public Long getId() {
		return id;
	}

	public String getSeatLabel() {
		return seatLabel;
	}

	public int getRow() {
		return row;
	}

	public char getColumn() {
		return column;
	}

	public int getZone() {
		return zone;
	}

	public BigDecimal getCalculatedPrice() {
		return calculatedPrice;
	}

	public List<LocalDateTime> getBookedSlots() {
		return bookedSlots;
	}

	/**
	 * WHY isAvailableFor(LocalDateTime) instead of isAvailable()? Availability
	 * depends on WHEN — not just true/false globally. "Is seat 3C free for the 7pm
	 * show on March 20?"
	 */
	public boolean isAvailableFor(LocalDateTime showDateTime) {
		return !bookedSlots.contains(showDateTime);
	}

	/**
	 * WHY bookSlot() instead of setAvailable(false)? bookSlot() adds a specific
	 * time — seat can still be free for other times. setAvailable(false) would
	 * block ALL times — wrong for multi-show rooms.
	 */
	public void bookSlot(LocalDateTime showDateTime) {
		bookedSlots.add(showDateTime);
	}

	/**
	 * WHY cancelSlot()? When a booking is cancelled, that specific time slot
	 * becomes available again. Other bookings for this seat at different times are
	 * unaffected.
	 */
	public void cancelSlot(LocalDateTime showDateTime) {
		bookedSlots.remove(showDateTime);
	}
}