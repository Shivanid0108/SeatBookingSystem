package module;

import java.math.BigDecimal;

/**
 * ResellListing represents a seat that a ClientUser has listed for resale.
 * 
 * REAL WORLD ANALOGY: Like StubHub or Ticketmaster resale — you bought a ticket
 * but can't go, so you list it for others to buy, sometimes at a discount.
 * 
 * FROM ASSIGNMENT: "Users can resell the seat by posting it in a sell list,
 * which includes the seat's business type, seat number, cost, and discount
 * ratio."
 * 
 * WHY separate from Booking? A ResellListing IS created FROM a Booking, but
 * it's a different concept: - Booking = "I have reserved this seat" -
 * ResellListing = "I want to sell my reservation to someone else"
 * 
 * When a ResellListing is created: 1. Original Booking status → RESOLD 2. New
 * ResellListing created with the seat details 3. Other users can see and buy
 * from the resell list
 * 
 * RELATIONSHIPS: ResellListing HAS-ONE Booking (original booking being resold)
 * ResellListing HAS-ONE Seat (the seat being resold) ResellListing HAS-ONE
 * ClientUser (the seller)
 * 
 * SPRING BOOT NOTE: - @Entity - @ManyToOne on booking, seat, seller
 */
public class ResellListing {

	/**
	 * WHY Long id? Spring Boot JPA standard.
	 */
	private Long id;

	/**
	 * WHY store the original Booking? We need to know WHICH booking is being
	 * resold. When someone buys from resell list: 1. Original
	 * booking.setStatus(RESOLD) — already done when listing created 2. New Booking
	 * created for the buyer 3. This ResellListing is removed from the list
	 */
	private Booking booking;

	/**
	 * WHY store Seat separately if we have Booking? Convenience — quick access to
	 * seat details without going through booking. booking.getSeat() works too, but
	 * direct reference is cleaner.
	 * 
	 * From assignment: "sell list includes seat's business type, seat number"
	 */
	private Seat seat;

	/**
	 * WHY store seller separately if we have Booking? Same reason — quick access.
	 * Also: when buyer messages seller, we need the seller reference directly. From
	 * assignment: "Users can communicate via messaging and bargain for seat prices"
	 */
	private ClientUser seller;

	/**
	 * WHY String businessType? From assignment: "sell list includes the seat's
	 * business type" Business type = what kind of venue (Cinema, Restaurant,
	 * Conference Room) This comes from Venue.getType() Stored here for quick
	 * display without loading the whole Venue.
	 */
	private String businessType;

	/**
	 * WHY BigDecimal resellPrice? The resell price might be different from the
	 * original calculatedPrice. Seller might add markup or give discount.
	 * 
	 * In Spring Boot DB: DECIMAL(10,2) column.
	 */
	private BigDecimal resellPrice;

	/**
	 * WHY BigDecimal discountRatio? From assignment: "sell list includes cost and
	 * discount ratio" discount ratio = how much discount the seller is offering
	 * Example: 0.10 = 10% discount from original price
	 * 
	 * resellPrice = originalPrice × (1 - discountRatio)
	 */
	private BigDecimal discountRatio;

	/**
	 * Constructor — called by ResellService when a client lists a seat for resale.
	 * 
	 * WHY these parameters? - booking: the original booking being resold - seller:
	 * who is selling (from the booking) - resellPrice: what price they want to sell
	 * at - discountRatio: what discount they're offering
	 * 
	 * businessType and seat are extracted from booking automatically.
	 */
	public ResellListing(Booking booking, ClientUser seller, BigDecimal resellPrice, BigDecimal discountRatio) {
		this.booking = booking;
		this.seller = seller;
		this.resellPrice = resellPrice;
		this.discountRatio = discountRatio;

		/**
		 * WHY extract these from booking? Convenience fields — stored directly for
		 * quick access. Avoids chaining:
		 * resellListing.getBooking().getSeat().getSeatLabel() Instead:
		 * resellListing.getSeat().getSeatLabel()
		 */
		this.seat = booking.getSeat();
		this.businessType = ""; // will be set from Venue.getType() in ResellService
	}

	// Getters — ResellListing fields don't change after creation
	public Long getId() {
		return id;
	}

	public Booking getBooking() {
		return booking;
	}

	public Seat getSeat() {
		return seat;
	}

	public ClientUser getSeller() {
		return seller;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public BigDecimal getResellPrice() {
		return resellPrice;
	}

	// Setter for price — seller might negotiate and update price
	public void setResellPrice(BigDecimal resellPrice) {
		this.resellPrice = resellPrice;
	}

	public BigDecimal getDiscountRatio() {
		return discountRatio;
	}

	/**
	 * WHY toString()? For displaying listing details to users browsing the resell
	 * market.
	 */
	@Override
	public String toString() {
		return "ResellListing {" + "Seat: " + seat.getSeatLabel() + ", Type: " + businessType + ", Price: "
				+ resellPrice + ", Discount: " + discountRatio + "%" + ", Seller: " + seller.getName() + "}";
	}
}