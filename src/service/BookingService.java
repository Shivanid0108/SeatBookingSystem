package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import module.Booking;
import module.BookingStatus;
import module.ClientUser;
import module.Seat;
import module.Show;

/**
 * WHAT: Manages seat booking operations — reserving, cancelling, and querying
 * bookings.
 *
 * WHY HASHMAP<String, List<Booking>> instead of List<Booking>? Key = client
 * email, Value = list of their bookings. Most common query: "show me all
 * bookings for THIS user" HashMap by email → O(1) to get a user's bookings vs
 * O(n) linear search.
 *
 * WHY NOT store bookings only in ClientUser.bookings? ClientUser.bookings =
 * personal history (client's perspective) BookingService.bookings = system
 * management (admin/service perspective) Two storage points serve different
 * purposes — not duplication.
 *
 * SHOW-BASED BOOKING (key design decision): Before: client entered
 * LocalDateTime manually → any date, valid or not After: client picks from
 * BusinessOwner-created Show objects → controlled reserveSeat() takes Show, not
 * LocalDateTime — enforces this constraint.
 *
 * SPRING BOOT MIGRATION: HashMap → BookingRepository bookings.get(email) →
 * bookingRepository.findByUserEmail(email) Business logic stays identical.
 */
public class BookingService {

	/**
	 * WHY email as key? Every booking query starts with "whose bookings?" → email
	 * identifies the user. O(1) retrieval of all bookings for a specific user.
	 *
	 * WHY List<Booking> as value? One user can have many bookings — one-to-many
	 * relationship. List preserves chronological order of bookings.
	 */
	private HashMap<String, List<Booking>> bookings = new HashMap<>();

	// ── Core booking operations ────────────────────────────────────────────

	/**
	 * WHY Show instead of LocalDateTime? Shows are created by BusinessOwner — only
	 * valid, future show times exist. Client picks from the list — no arbitrary
	 * date entry possible. Show object gives us: showName, room, showDateTime in
	 * one package.
	 *
	 * WHY check show.isActive() first? BusinessOwner might cancel a show between
	 * when client views it and books. Always verify the show is still active before
	 * completing the booking.
	 *
	 * WHAT HAPPENS on successful booking: 1. Booking object created (status =
	 * CONFIRMED automatically) 2. Seat slot marked as booked (prevents double
	 * booking for this show time) 3. Added to client's personal history
	 * (ClientUser.bookings) 4. Added to service HashMap (system-wide management)
	 *
	 * WHY computeIfAbsent? First booking for a new client → no list exists yet in
	 * HashMap. computeIfAbsent creates the list if missing, then adds the booking.
	 * Cleaner than: if(!bookings.containsKey(email)) bookings.put(email, new
	 * ArrayList<>())
	 */
	public String reserveSeat(ClientUser client, Seat seat, Show show) {
		if (!show.isActive())
			return "This show has been cancelled";

		if (!seat.isAvailableFor(show.getShowDateTime()))
			return "Seat " + seat.getSeatLabel() + " is already booked for this show";

		Booking booking = new Booking(seat, client, show);

		// Mark this time slot as booked in the Seat — prevents double booking
		seat.bookSlot(show.getShowDateTime());

		// Add to client's personal booking history
		client.addBooking(booking);

		// Add to service-level HashMap for system management
		bookings.computeIfAbsent(client.getEmail(), k -> new ArrayList<>()).add(booking);

		return "Booking confirmed! Seat " + seat.getSeatLabel() + " for " + show.getShowName() + " | Price: $"
				+ seat.getCalculatedPrice();
	}

	/**
	 * WHY check existence before cancelling? Prevents cancelling a booking that was
	 * already cancelled or doesn't exist. Could happen if client tries to cancel
	 * from stale data.
	 *
	 * WHY check CONFIRMED status? CANCELLED → already cancelled, can't cancel again
	 * RESOLD → sold to someone else, no longer client's to cancel Only CONFIRMED
	 * bookings can be cancelled.
	 *
	 * WHAT HAPPENS on cancellation: 1. Status set to CANCELLED (keeps record,
	 * booking not deleted) 2. Seat slot freed (another client can now book this
	 * show time) 3. Booking removed from service HashMap (no longer active)
	 *
	 * NOTE: From assignment — client loses money on cancellation unless they sell
	 * or exchange their seat (use resellSeat instead).
	 */
	public String cancelBooking(Booking booking) {
		List<Booking> clientBookings = bookings.get(booking.getUser().getEmail());
		if (clientBookings == null || !clientBookings.contains(booking))
			return "Booking not found";
		if (booking.getStatus() != BookingStatus.CONFIRMED)
			return "Only confirmed bookings can be cancelled";

		booking.setStatus(BookingStatus.CANCELLED);

		// Free up this specific time slot — other shows unaffected
		booking.getSeat().cancelSlot(booking.getShowDateTime());

		clientBookings.remove(booking);
		return "Booking cancelled successfully";
	}

	// ── Query operations ───────────────────────────────────────────────────

	/**
	 * WHY return new ArrayList<>() instead of null when no bookings? Returning null
	 * forces every caller to null-check before using result. Empty list:
	 * for(Booking b : getBookingsByUser(client)) works safely even if empty. This
	 * is the "null object pattern" — return a safe empty collection, not null.
	 */
	public List<Booking> getBookingbyUser(ClientUser client) {
		List<Booking> list = bookings.get(client.getEmail());
		return list != null ? list : new ArrayList<>();
	}

	/**
	 * Find a specific booking by seat AND show. Used when client wants to resell a
	 * specific booking. Returns null if not found — caller must null-check.
	 */
	public Booking findBySeatAndShow(ClientUser client, Seat seat, Show show) {
		List<Booking> list = bookings.get(client.getEmail());
		if (list == null)
			return null;
		for (Booking b : list)
			if (b.getSeat().getSeatLabel().equals(seat.getSeatLabel()) && b.getShow().equals(show))
				return b;
		return null;
	}
}