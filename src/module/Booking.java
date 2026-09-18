package module;

/**
 * WHAT: Represents one reservation — a ClientUser booking a Seat for a specific
 * Show.
 *
 * ANALOGY: Like a cinema ticket — it says who (user), where (seat), when
 * (show).
 *
 * WHY Show instead of LocalDateTime? Before: client entered date/time manually
 * → error prone, no validation. After: client picks from BusinessOwner-created
 * shows → controlled and clean. Show contains: showName, room, showDateTime —
 * richer than a raw timestamp.
 *
 * RELATIONSHIPS: - Booking HAS-ONE Seat (one booking is for one specific seat)
 * - Booking HAS-ONE ClientUser (one booking belongs to one client) - Booking
 * HAS-ONE Show (one booking is for one specific show) - ClientUser HAS-MANY
 * Bookings
 *
 * BOOKING LIFECYCLE: Created → CONFIRMED Cancelled → CANCELLED (seat freed)
 * Resold → RESOLD (ResellListing created, new booking for buyer)
 *
 * SPRING BOOT NOTE: - @Entity - @ManyToOne on seat, user, show
 * - @Enumerated(EnumType.STRING) on status
 */
public class Booking {

	private Long id;

	/**
	 * WHY store full Seat object not just seatLabel String? Full object gives
	 * instant access to all seat info: seat.getSeatLabel(),
	 * seat.getCalculatedPrice(), seat.getZone() Storing just "3C" loses all this
	 * information.
	 *
	 * SPRING BOOT: @ManyToOne @JoinColumn(name = "seat_id")
	 */
	private Seat seat;

	/**
	 * WHY ClientUser specifically, not just User? Only ClientUsers can make
	 * bookings — not Admins or BusinessOwners. Using ClientUser type enforces this
	 * at compile time. Passing an Admin object here won't compile.
	 *
	 * SPRING BOOT: @ManyToOne @JoinColumn(name = "user_id")
	 */
	private ClientUser user;

	/**
	 * WHY Show instead of LocalDateTime? Show gives us showName + room + dateTime
	 * in one object. More informative than a raw timestamp. BusinessOwner controls
	 * which shows exist — no arbitrary times.
	 *
	 * SPRING BOOT: @ManyToOne @JoinColumn(name = "show_id")
	 */
	private Show show;

	/**
	 * WHY enum BookingStatus instead of String? Prevents typos: "Confirmd" compiles
	 * as String, fails at runtime. BookingStatus.CONFIRMD won't compile — caught
	 * immediately.
	 *
	 * SPRING BOOT: @Enumerated(EnumType.STRING)
	 */
	private BookingStatus status;

	/**
	 * WHY status = CONFIRMED automatically? Every new booking starts confirmed. It
	 * only changes if cancelled (CANCELLED) or resold (RESOLD). Caller never needs
	 * to pass status — always CONFIRMED at creation.
	 */
	public Booking(Seat seat, ClientUser user, Show show) {
		this.seat = seat;
		this.user = user;
		this.show = show;
		this.status = BookingStatus.CONFIRMED;
	}

	public Long getId() {
		return id;
	}

	public Seat getSeat() {
		return seat;
	}

	public ClientUser getUser() {
		return user;
	}

	public Show getShow() {
		return show;
	}

	public BookingStatus getStatus() {
		return status;
	}

	/**
	 * WHY setter only for status? Status is the only thing that changes after
	 * booking creation. Seat, user, and show never change — once booked, those
	 * details are fixed.
	 */
	public void setStatus(BookingStatus status) {
		this.status = status;
	}

	/**
	 * Convenience method — most callers need the raw LocalDateTime for seat slot
	 * operations. Gets it from the Show object.
	 */
	public java.time.LocalDateTime getShowDateTime() {
		return show.getShowDateTime();
	}

	@Override
	public String toString() {
		return "Booking { Seat: " + seat.getSeatLabel() + " | Show: " + show.getShowName() + " | "
				+ show.getShowDateTime() + " | Status: " + status + " }";
	}
}