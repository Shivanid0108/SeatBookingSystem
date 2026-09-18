package module;

/**
 * WHAT: Enum defining all valid states a Booking can be in.
 *
 * WHY ENUM instead of String? String approach (BAD):
 * booking.setStatus("Confirmed") → typo "Confirmd" compiles fine, fails at
 * runtime
 *
 * Enum approach (GOOD): booking.setStatus(BookingStatus.CONFIRMED) → typo won't
 * compile, caught immediately
 *
 * WHY THESE THREE VALUES? CONFIRMED → booking just created, seat reserved,
 * client paid CANCELLED → client cancelled; they lose money unless they resell
 * (from assignment: "users will lose money unless they sell or exchange seats")
 * RESOLD → client listed this booking on the resell marketplace original
 * booking marked RESOLD, new booking created for buyer
 *
 * LIFECYCLE: new Booking() → CONFIRMED ↓ cancelBooking() → CANCELLED (seat
 * freed up) OR resellSeat() → RESOLD (ResellListing created) ↓ buySeat() → new
 * CONFIRMED booking for buyer
 *
 * SPRING BOOT NOTE:
 * 
 * @Enumerated(EnumType.STRING) stores "CONFIRMED", "CANCELLED", "RESOLD" as
 *                              readable strings in DB instead of ordinal
 *                              numbers (0, 1, 2). STRING preferred over ORDINAL
 *                              because: - Readable directly in DB - Adding new
 *                              values won't break existing data
 */
public enum BookingStatus {
	CONFIRMED, CANCELLED, RESOLD
}