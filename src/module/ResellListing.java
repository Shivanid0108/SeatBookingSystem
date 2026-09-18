package module;

import java.math.BigDecimal;

/**
 * WHAT: Represents a seat that a ClientUser has listed for resale on the
 * marketplace.
 *
 * ANALOGY: Like StubHub — you bought a ticket but can't go, so you list it for
 * others.
 *
 * FROM ASSIGNMENT: "Users can resell the seat by posting it in a sell list,
 * which includes the seat's business type, seat number, cost, and discount
 * ratio."
 *
 * WHY SEPARATE FROM BOOKING? A Booking = "I have reserved this seat" A
 * ResellListing = "I want to sell my reservation to someone else" When created:
 * original Booking.status → RESOLD, new ResellListing created. When bought:
 * ResellListing removed, new Booking created for buyer.
 *
 * RELATIONSHIPS: - ResellListing HAS-ONE Booking (original booking being
 * resold) - ResellListing HAS-ONE Seat (the seat being resold — convenience
 * reference) - ResellListing HAS-ONE ClientUser (the seller)
 *
 * SPRING BOOT NOTE: - @Entity - @ManyToOne on booking, seat, seller
 */
public class ResellListing {

	private Long id;

	/**
	 * WHY store the original Booking? Need to know WHICH booking is being resold.
	 * When someone buys: original booking.status = RESOLD (already done), new
	 * Booking created for buyer, this ResellListing removed.
	 */
	private Booking booking;

	/**
	 * WHY store Seat separately if we have Booking? Convenience — quick access
	 * without chaining: resellListing.getSeat() vs
	 * resellListing.getBooking().getSeat() Also: ResellService stores listings by
	 * zone → needs seat.getZone() frequently.
	 */
	private Seat seat;

	/**
	 * WHY store seller separately if we have Booking? Quick access for: "can't buy
	 * your own listing" check. Also: when buyer messages seller, direct reference
	 * needed.
	 */
	private ClientUser seller;

	/**
	 * WHY String businessType? From assignment: "sell list includes the seat's
	 * business type" Business type = what kind of venue (Cinema, Restaurant, etc.)
	 * Stored here for quick display without loading the whole Venue.
	 */
	private String businessType;

	/**
	 * WHY BigDecimal resellPrice? Seller sets their own price — may differ from
	 * original calculatedPrice. Seller might mark up (rare) or discount (common to
	 * sell quickly).
	 */
	private BigDecimal resellPrice;

	/**
	 * WHY BigDecimal discountRatio? From assignment: "sell list includes cost and
	 * discount ratio" discountRatio = 0.10 means 10% discount from original price.
	 * Range: 0.00 (no discount) to 1.00 (100% discount = free).
	 */
	private BigDecimal discountRatio;

	/**
	 * Constructor — called by ResellService.resellSeat(). seat and businessType
	 * extracted from booking automatically.
	 */
	public ResellListing(Booking booking, ClientUser seller, BigDecimal resellPrice, BigDecimal discountRatio) {
		this.booking = booking;
		this.seller = seller;
		this.resellPrice = resellPrice;
		this.discountRatio = discountRatio;
		this.seat = booking.getSeat();
		this.businessType = ""; // set by ResellService from Venue.getType()
	}

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

	public BigDecimal getResellPrice() {
		return resellPrice;
	}

	public BigDecimal getDiscountRatio() {
		return discountRatio;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	// Seller might negotiate price — update if buyer makes offer
	public void setResellPrice(BigDecimal resellPrice) {
		this.resellPrice = resellPrice;
	}

	@Override
	public String toString() {
		return "ResellListing { Seat: " + seat.getSeatLabel() + " | Type: " + businessType + " | Price: $" + resellPrice
				+ " | Discount: " + discountRatio.multiply(new BigDecimal("100")) + "%" + " | Seller: "
				+ seller.getName() + " }";
	}
}