package module;

/**
 * BusinessOwner extends User — BusinessOwner IS-A User.
 * 
 * WHY does BusinessOwner exist as a separate class? Because BusinessOwner has
 * DIFFERENT permissions and actions than Admin or ClientUser: - Can create
 * Venues - Can add Rooms to their Venue - Can set regular price - Can view
 * bookings for their venue
 * 
 * These actions will be in VenueService.java — not here.
 * 
 * FUTURE FIELDS (when implementing Spring Boot): BusinessOwner might get
 * additional fields like: - private List<Venue> venues (venues they own) -
 * private String businessName - private String businessLicense
 * 
 * We keep it simple for now and add fields as needed.
 */
public class BusinessOwner extends User {

	/**
	 * Same pattern as Admin — role is hardcoded to "BusinessOwner". Caller just
	 * passes name, email, password. Role is set automatically — no room for error.
	 */
	public BusinessOwner(String name, String email, String password) {
		super(name, email, password, "BusinessOwner");
	}
}