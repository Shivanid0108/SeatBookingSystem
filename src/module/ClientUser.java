package module;

import java.util.ArrayList;
import java.util.List;

/**
 * ClientUser extends User — ClientUser IS-A User.
 * 
 * WHY is it called ClientUser and not just Client? To avoid confusion with
 * other "Client" concepts (like HTTP clients, DB clients). ClientUser clearly
 * means "a user who is a client of the booking system".
 * 
 * WHAT makes ClientUser different from Admin and BusinessOwner? ClientUser has
 * additional fields specific to their role: - bookings: list of seats they have
 * booked - watchList: list of seats they are watching (for notifications) -
 * balance: their account balance (commented out for now, will add with DB)
 * 
 * These fields don't exist in Admin or BusinessOwner — that's why ClientUser is
 * its own subclass and not just a role string on User.
 */
public class ClientUser extends User {

	/**
	 * WHY List<Booking> here in ClientUser?
	 * 
	 * A ClientUser HAS-MANY Bookings — one-to-many relationship. This will become
	 * a @OneToMany relationship in Spring Boot JPA.
	 * 
	 * We store bookings here so we can easily answer: "Show me all bookings for
	 * this user" → user.getBookings() Without having to search through all bookings
	 * in the system.
	 */
	private List<Booking> bookings = new ArrayList<>();

	/**
	 * WHY List<Seat> watchList?
	 * 
	 * From assignment requirement 6: "When a new seat is added to the sell list,
	 * all users who are watching the service seat will be notified."
	 * 
	 * A user can "watch" a seat — when that seat gets listed for resell, they get a
	 * notification automatically. This is the Observer pattern — ClientUser
	 * observes Seats.
	 */
	private List<Seat> watchList = new ArrayList<>();

	/**
	 * WHY is balance commented out? Balance requires BigDecimal for accurate money
	 * math. We'll uncomment and implement it when we add payment logic. Keeping it
	 * commented (not deleted) reminds us it needs to be added later.
	 * 
	 * In Spring Boot: private BigDecimal balance; Will map to a DECIMAL column in
	 * DB.
	 */
	// private BigDecimal balance;

	/**
	 * Constructor — same pattern as Admin and BusinessOwner. Role is hardcoded to
	 * "ClientUser" automatically.
	 */
	public ClientUser(String name, String email, String password) {
		super(name, email, password, "ClientUser");
	}

	/**
	 * WHY addBooking() instead of just getBookings().add()? Encapsulation — we
	 * control HOW bookings are added. Later we can add validation here: e.g. "check
	 * if user already has a booking for this time slot" Without changing any other
	 * code.
	 */
	public void addBooking(Booking booking) {
		bookings.add(booking);
	}

	public List<Booking> getBookings() {
		return bookings;
	}

	/**
	 * WHY addToWatchList() separately? Same reason — controlled access. Later we
	 * can check "is this seat already in watchList?" before adding.
	 */
	public void addToWatchList(Seat seat) {
		watchList.add(seat);
	}

	public List<Seat> getWatchList() {
		return watchList;
	}
}