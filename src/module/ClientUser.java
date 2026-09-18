package module;

import java.util.ArrayList;
import java.util.List;

/**
 * WHAT: Represents a client who browses shows, books seats, and uses the resell
 * market.
 *
 * WHY EXTRA FIELDS compared to Admin and BusinessOwner? ClientUser has
 * fundamentally different responsibilities — they USE the system: - Books seats
 * for shows - Cancels bookings - Resells bookings on the marketplace - Buys
 * from the resell market - Watches seats for resell notifications - Messages
 * other users to bargain prices
 *
 * Admin and BusinessOwner don't need booking history or watchlists.
 *
 * SPRING BOOT NOTE: - @Entity @DiscriminatorValue("ClientUser") - @OneToMany on
 * bookings (one client has many bookings) - @ManyToMany on watchList (many
 * clients watch many seats)
 */
public class ClientUser extends User {

	/**
	 * WHY List<Booking> here in ClientUser? ClientUser HAS-MANY Bookings —
	 * one-to-many relationship. Storing here allows: client.getBookings() without
	 * searching the entire system.
	 *
	 * ALSO stored in BookingService HashMap for system-wide operations. Two storage
	 * points serve different purposes: - ClientUser.bookings → personal history
	 * (client's perspective) - BookingService.bookings → system management (service
	 * perspective)
	 */
	private List<Booking> bookings = new ArrayList<>();

	/**
	 * WHY watchList? From assignment requirement 6: "When a seat is added to the
	 * sell list, watching users get notified." A client can watch a seat — when it
	 * gets listed for resell, they're notified. This is the Observer pattern —
	 * ClientUser observes Seats.
	 */
	private List<Seat> watchList = new ArrayList<>();

	/**
	 * Constructor — same pattern as Admin and BusinessOwner. Role hardcoded to
	 * "ClientUser" automatically.
	 */
	public ClientUser(String name, String email, String password) {
		super(name, email, password, "ClientUser");
	}

	/**
	 * WHY addBooking() instead of getBookings().add()? Encapsulation — we control
	 * HOW bookings are added. Later we can add validation here (e.g. max bookings
	 * per user) without changing any other code.
	 */
	public void addBooking(Booking booking) {
		bookings.add(booking);
	}

	public List<Booking> getBookings() {
		return bookings;
	}

	public void addToWatchList(Seat seat) {
		watchList.add(seat);
	}

	public List<Seat> getWatchList() {
		return watchList;
	}
}