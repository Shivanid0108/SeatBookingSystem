package module;

/**
 * WHAT: Represents a business owner who registers venues and schedules shows.
 *
 * WHY EXISTS AS SEPARATE CLASS FROM ADMIN? BusinessOwner has completely
 * different permissions: - Creates and manages venues - Adds rooms to venues -
 * Schedules shows (date + time) - Sets seat pricing
 *
 * Admin manages the SYSTEM. BusinessOwner manages their own VENUES.
 *
 * ANALOGY (Airbnb): BusinessOwner = the host who lists properties Admin =
 * Airbnb itself (manages the platform) ClientUser = the guest who books
 *
 * FUTURE FIELDS (Spring Boot migration): Could add: private List<Venue> venues
 * (venues they own) For now, VenueService holds the venue list in memory.
 *
 * SPRING BOOT NOTE: Will get @Entity and @DiscriminatorValue("BusinessOwner")
 */
public class BusinessOwner extends User {

	/**
	 * Same pattern as Admin — role hardcoded to "BusinessOwner". Caller just passes
	 * name, email, password.
	 */
	public BusinessOwner(String name, String email, String password) {
		super(name, email, password, "BusinessOwner");
	}
}