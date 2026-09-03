package module;

/**
 * BookingStatus is an ENUM — a fixed set of allowed values for booking status.
 * 
 * WHY enum instead of String constants?
 * 
 * String approach (BAD): booking.setStatus("Confirmed"); // what if someone
 * types "confirmed" or "CONFIRMED"? booking.setStatus("Cancled"); // typo! no
 * compile error, bug at runtime
 * 
 * Enum approach (GOOD): booking.setStatus(BookingStatus.CONFIRMED); // only
 * valid values compile booking.setStatus(BookingStatus.CANCLED); // compile
 * error! caught immediately
 * 
 * WHY these three values?
 * 
 * CONFIRMED: Booking was just created successfully. Seat slot is now reserved
 * for this client.
 * 
 * CANCELLED: Client cancelled their booking. From assignment: "Users can change
 * or cancel their booking; however, they will lose money unless they sell or
 * exchange their seats." When cancelled, the slot is freed up in the seat.
 * 
 * RESOLD: Client listed this booking on the resell market. From assignment:
 * "Users can resell the seat by posting it in a sell list." The booking still
 * exists but is being transferred to another buyer.
 * 
 * SPRING BOOT NOTE:
 * 
 * @Enumerated(EnumType.STRING) will store "CONFIRMED", "CANCELLED", "RESOLD" as
 *                              readable strings in the DB instead of numbers
 *                              (0, 1, 2). STRING is preferred over ORDINAL
 *                              because: - Readable in DB directly - Adding new
 *                              values won't break existing data
 */
public enum BookingStatus {
	CONFIRMED, CANCELLED, RESOLD
}