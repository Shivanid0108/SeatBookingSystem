package module;

import java.time.LocalDateTime;

/**
 * WHAT: Represents one specific screening/event scheduled in a Room. Examples:
 * "Avengers 7pm Show", "Comedy Night", "Board Meeting A"
 *
 * WHY SHOW EXISTS: Before Show: clients entered date/time manually — error
 * prone, no validation, bookings for non-existent times possible. After Show:
 * BusinessOwner creates shows with specific times. Clients PICK from available
 * shows — controlled and clean.
 *
 * ANALOGY: Like a cinema listing — the theatre sets the showtimes, you pick
 * from what's available, not enter a random time yourself.
 *
 * RELATIONSHIPS: - Room HAS-MANY Shows (one room can have multiple shows per
 * day) - Show HAS-ONE Room (each show is in one specific room) - Booking
 * references a Show (not a raw LocalDateTime)
 *
 * SPRING BOOT NOTE: - @Entity - @ManyToOne on room (many shows happen in one
 * room)
 */
public class Show {

	private Long id;

	/**
	 * Human-readable name set by BusinessOwner. Examples: "Avengers - Evening
	 * Show", "Conference Room A - 10am"
	 */
	private String showName;

	/**
	 * Which room/screen this show is in. Show belongs to one Room — Room can have
	 * many Shows.
	 *
	 * SPRING BOOT: @ManyToOne @JoinColumn(name = "room_id")
	 */
	private Room room;

	/**
	 * Exact date and time of this show. Set ONLY by BusinessOwner — client never
	 * enters this manually. This is the key design decision that replaced free-form
	 * date entry.
	 *
	 * SPRING BOOT: maps to DATETIME or TIMESTAMP column.
	 */
	private LocalDateTime showDateTime;

	/**
	 * WHY isActive? BusinessOwner can cancel a show (e.g. film cancelled, event
	 * postponed). We don't delete it — existing bookings still reference it. We
	 * mark it inactive so clients can no longer select it. Starts as true (active)
	 * when created.
	 */
	private boolean isActive;

	public Show(String showName, Room room, LocalDateTime showDateTime) {
		this.showName = showName;
		this.room = room;
		this.showDateTime = showDateTime;
		this.isActive = true;
	}

	public Long getId() {
		return id;
	}

	public String getShowName() {
		return showName;
	}

	public Room getRoom() {
		return room;
	}

	public LocalDateTime getShowDateTime() {
		return showDateTime;
	}

	public boolean isActive() {
		return isActive;
	}

	// Setter for showName — BusinessOwner might rename the show
	public void setShowName(String showName) {
		this.showName = showName;
	}

	// Setter for isActive — used when BusinessOwner cancels a show
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public String toString() {
		return showName + " | " + showDateTime.toLocalDate() + " at " + showDateTime.toLocalTime()
				+ (isActive ? "" : " [CANCELLED]");
	}
}