package service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import module.BusinessOwner;
import module.Room;
import module.Show;
import module.VenuType;
import module.Venue;

/**
 * WHAT: Manages venue operations — creation, room management, and show
 * scheduling.
 *
 * WHO USES THIS? - BusinessOwner: creates venues, adds rooms, schedules shows,
 * updates pricing - AdminService: reads venue list for admin overview -
 * Main.java: calls methods based on menu selections
 *
 * WHY LIST instead of HashMap for venues? Venues are few (a BusinessOwner has
 * 2-5 venues max). Linear search through 5 venues is instant — HashMap overhead
 * not justified. Contrast with UserService: thousands of users → HashMap
 * critical.
 *
 * SHOW SCHEDULING DESIGN DECISION: BusinessOwner creates shows with specific
 * date/time. Clients PICK from available shows — never enter date/time
 * manually. This prevents bookings for non-existent or past show times.
 *
 * SPRING BOOT MIGRATION: List<Venue> venues → VenueRepository venues.add(venue)
 * → venueRepository.save(venue) getVenues() → venueRepository.findAll()
 * Business logic stays identical.
 */
public class VenueService {

	/**
	 * WHY List not HashMap? Few venues per system — linear search is fast enough.
	 * No frequent "find by key" operations like UserService has. List preserves
	 * insertion order — venues display in creation order.
	 */
	private List<Venue> venues = new ArrayList<>();

	// ── Venue operations ───────────────────────────────────────────────────

	/**
	 * WHY BusinessOwner as parameter? Type safety — only BusinessOwner can create
	 * venues. Java enforces this at compile time: passing Admin won't compile. No
	 * extra role check needed inside the method.
	 *
	 * VALIDATION ORDER matters: 1. Name empty check (cheapest) 2. Price check
	 * (cheap) 3. Duplicate check (searches list — most expensive) Always validate
	 * cheap operations first.
	 */
	public String createVenue(BusinessOwner owner, String name, VenuType type, BigDecimal price) {
		if (name == null || name.trim().isEmpty())
			return "Venue name cannot be empty";
		if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
			return "Price must be greater than zero";
		if (findVenueByName(name) != null)
			return "Venue with this name already exists";
		venues.add(new Venue(name, type, price));
		return "Venue created successfully!";
	}

	/**
	 * WHY rows and cols separately instead of one roomSize? Original design: square
	 * rooms only, max 26x26. Real venues aren't square — a sports arena has 200
	 * rows x 26 cols. Separating rows/cols removes the artificial square
	 * constraint. Max cols = 26 (A-Z alphabet limit for seat labels). Max rows =
	 * 200 (reasonable real-world maximum).
	 *
	 * WHY get price from venue.getRegularPrice()? Room needs the price to calculate
	 * zone pricing for each seat. We get it from the venue — no need to pass it
	 * separately. This avoids circular dependency: VenueService knows Venue, not
	 * vice versa.
	 */
	public String addRoom(Venue venue, int rows, int cols) {
		if (rows <= 0 || cols <= 0)
			return "Rows and columns must be greater than 0";
		if (cols > 26)
			return "Columns cannot exceed 26 (A-Z)";
		if (rows > 200)
			return "Rows cannot exceed 200";
		venue.addRoom(new Room(rows, cols, venue.getRegularPrice()));
		return "Room added! (" + rows + " rows x " + cols + " cols = " + (rows * cols) + " seats)";
	}

	// ── Show operations ────────────────────────────────────────────────────

	/**
	 * WHY is createShow() in VenueService and not a separate ShowService? Shows are
	 * tightly coupled to Rooms (a show IS in a room). VenueService already manages
	 * Rooms — shows are a natural extension. Adding a ShowService would create
	 * unnecessary indirection.
	 *
	 * WHY future date check? Prevents creating shows for dates that have already
	 * passed. A show on March 20, 2020 makes no sense to create today.
	 *
	 * WHY duplicate time check? Two shows can't start at the same time in the same
	 * room. A cinema screen can only show one film at a time.
	 */
	public String createShow(Room room, String showName, LocalDateTime showDateTime) {
		if (showName == null || showName.trim().isEmpty())
			return "Show name cannot be empty";
		if (showDateTime == null)
			return "Show date and time cannot be empty";
		if (showDateTime.isBefore(LocalDateTime.now()))
			return "Show time must be in the future";

		// Check for duplicate show time in same room
		for (Show existing : room.getAllShows())
			if (existing.getShowDateTime().equals(showDateTime))
				return "A show already exists in this room at that time";

		room.addShow(new Show(showName, room, showDateTime));
		return "Show created: " + showName + " at " + showDateTime;
	}

	/**
	 * WHY setActive(false) instead of deleting the show? Existing bookings
	 * reference this show object. Deleting the show would break those booking
	 * references. Marking inactive: show disappears from client view, bookings
	 * still valid. BusinessOwner can see cancelled shows in their full schedule
	 * view.
	 */
	public String cancelShow(Show show) {
		if (!show.isActive())
			return "Show is already cancelled";
		show.setActive(false);
		return "Show cancelled: " + show.getShowName();
	}

	// ── Price and venue management ─────────────────────────────────────────

	public String updatePrice(Venue venue, BigDecimal newPrice) {
		if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0)
			return "Price must be greater than zero";
		venue.setRegularPrice(newPrice);
		return "Price updated to $" + newPrice;
	}

	public String deleteVenue(Venue venue) {
		venues.remove(venue);
		return "Venue deleted successfully";
	}

	public List<Venue> getVenues() {
		return venues;
	}

	/**
	 * WHY private? (unlike UserService.findUserByEmail which is public) Venues are
	 * searched internally within VenueService only (duplicate check). No external
	 * class needs to find a venue by name directly. External callers get venues via
	 * getVenues() and pick by index in UI.
	 *
	 * WHY equalsIgnoreCase? "cineplex" and "Cineplex" should be treated as
	 * duplicates. Case-insensitive prevents accidental duplicate venues.
	 */
	public Venue findVenueByName(String name) {
		for (Venue v : venues)
			if (v.getName().equalsIgnoreCase(name))
				return v;
		return null;
	}
}