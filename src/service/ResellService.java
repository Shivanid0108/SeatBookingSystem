package service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import module.Booking;
import module.BookingStatus;
import module.ClientUser;
import module.ResellListing;
import module.Seat;
import module.Show;

/**
 * WHAT: Manages the resell marketplace — listing seats for sale and purchasing
 * them.
 *
 * WHY HASHMAP<Integer, List<ResellListing>>? Key = zone number (1, 2, 3, or 4)
 * Value = list of listings in that zone
 *
 * WHY zone as key? Clients browsing the resell market often want seats by zone:
 * "Show me Zone 1 (front row) resell listings" HashMap by zone → O(1) to get
 * all listings for a zone.
 *
 * COMPARED TO other HashMaps in this project: UserService.users →
 * HashMap<email, User> (search by email) BookingService → HashMap<email,
 * Bookings> (search by user) ResellService → HashMap<zone, Listings> (search by
 * zone) Each uses the most natural search key for its use case.
 *
 * SPRING BOOT MIGRATION: HashMap → ResellListingRepository listings.get(zone) →
 * resellRepository.findByZone(zone) Business logic stays identical.
 */
public class ResellService {

	private HashMap<Integer, List<ResellListing>> listings = new HashMap<>();

	// ── Resell operations ──────────────────────────────────────────────────

	/**
	 * WHY check CONFIRMED only? CANCELLED → already cancelled, nothing to sell
	 * RESOLD → already sold to someone else CONFIRMED → active booking that can be
	 * resold
	 *
	 * WHY validate resellPrice and discountRatio? resellPrice must be > 0 (can't
	 * sell for free or negative) discountRatio must be 0.00-1.00 (0%=no discount,
	 * 100%=free, >100%=impossible)
	 *
	 * WHAT HAPPENS on successful resell listing: 1. ResellListing created with
	 * booking, seller, price, discount 2. Original booking.status → RESOLD (client
	 * gave up their seat) 3. Stored in HashMap by zone for O(1) zone-based browsing
	 *
	 * NOTE: Client's booking is now RESOLD — they can't cancel it anymore. They
	 * committed to selling. Buyer must complete the purchase.
	 */
	public String resellSeat(ClientUser seller, Booking booking, BigDecimal resellPrice, BigDecimal discountRatio) {
		if (booking.getStatus() != BookingStatus.CONFIRMED)
			return "Only confirmed bookings can be resold";
		if (resellPrice == null || resellPrice.compareTo(BigDecimal.ZERO) <= 0)
			return "Resell price must be greater than zero";
		if (discountRatio == null || discountRatio.compareTo(BigDecimal.ZERO) < 0
				|| discountRatio.compareTo(BigDecimal.ONE) > 0)
			return "Discount ratio must be between 0.00 and 1.00";

		ResellListing listing = new ResellListing(booking, seller, resellPrice, discountRatio);
		booking.setStatus(BookingStatus.RESOLD);

		// Store by zone — seat.getZone() = 1, 2, 3, or 4
		int zone = listing.getSeat().getZone();
		listings.computeIfAbsent(zone, k -> new ArrayList<>()).add(listing);

		return "Seat listed for resale! Zone " + zone + " | Price: $" + resellPrice;
	}

	/**
	 * WHY check buyer != seller? From assignment: prevents gaming the system
	 * (buying your own listing to manipulate prices or game any future reward
	 * system).
	 *
	 * WHY Show from booking instead of LocalDateTime? Booking now stores a Show
	 * object (not raw LocalDateTime). New booking for buyer must reference the SAME
	 * Show — same event. seat.bookSlot() still uses LocalDateTime internally via
	 * show.getShowDateTime().
	 *
	 * WHAT HAPPENS on successful purchase: 1. Get seat and show from the listing's
	 * original booking 2. Create NEW Booking for buyer (status = CONFIRMED
	 * automatically) 3. Mark seat as booked for this show time (already booked from
	 * seller's booking but seller's was RESOLD status — slot needs to stay blocked)
	 * 4. Add new booking to buyer's personal history 5. Remove listing from HashMap
	 * (no longer for sale)
	 */
	public String buySeat(ClientUser buyer, ResellListing listing) {
		if (listing.getSeller().equals(buyer))
			return "You cannot buy your own listing";

		Seat seat = listing.getSeat();

		// Get the Show from the original booking — Show not LocalDateTime
		Show show = listing.getBooking().getShow();

		// Create new Booking for buyer with the same Show
		Booking newBooking = new Booking(seat, buyer, show);

		// Seat slot stays blocked — just transferred from seller to buyer
		seat.bookSlot(show.getShowDateTime());

		buyer.addBooking(newBooking);

		// Remove from resell market
		int zone = seat.getZone();
		listings.get(zone).remove(listing);

		return "Seat purchased successfully! " + newBooking;
	}

	// ── Query operations ───────────────────────────────────────────────────

	/**
	 * WHY return empty list instead of null? Same reasoning as
	 * BookingService.getBookingsByUser() — empty list is safe to iterate, null
	 * causes NullPointerException.
	 */
	public List<ResellListing> getListingsByZone(int zone) {
		List<ResellListing> zoneListings = listings.get(zone);
		return zoneListings != null ? zoneListings : new ArrayList<>();
	}

	/**
	 * WHY addAll() in a loop? listings.values() returns
	 * Collection<List<ResellListing>> — a collection of lists. We want one flat
	 * List<ResellListing> — all listings regardless of zone. addAll() merges each
	 * zone's list into one result list.
	 *
	 * Example: Zone 1: [listing1, listing2] Zone 3: [listing3] getAllListings() →
	 * [listing1, listing2, listing3]
	 */
	public List<ResellListing> getAllListings() {
		List<ResellListing> all = new ArrayList<>();
		for (List<ResellListing> zoneListings : listings.values())
			all.addAll(zoneListings);
		return all;
	}
}