package module;

/**
 * WHAT: Represents a system administrator.
 *
 * WHY SO SIMPLE? Only a constructor? Admin shares all fields with User (name,
 * email, password, role). Admin-specific BEHAVIOUR lives in AdminService, not
 * here.
 *
 * SEPARATION OF CONCERNS: - Admin.java (model) = what an Admin IS (data) -
 * AdminService (service) = what an Admin can DO (behaviour)
 *
 * WHAT CAN ADMIN DO? (handled in AdminService) - View all users - Delete /
 * suspend users - View all venues - Handle complaints via messaging
 *
 * SPRING BOOT NOTE: Will get @Entity and @DiscriminatorValue("Admin") to map to
 * the correct row in the DB inheritance table.
 */
public class Admin extends User {

	/**
	 * WHY no "role" parameter? Admin ALWAYS has role "Admin" — no exceptions.
	 * Hardcoding it here means: 1. Caller can't accidentally pass the wrong role 2.
	 * No extra parameter needed — simpler API 3. Role is guaranteed to always be
	 * correct
	 *
	 * External caller: new Admin("John", "john@gmail.com", "Pass@123") Internally:
	 * super(name, email, password, "Admin") → role set automatically
	 */
	public Admin(String name, String email, String password) {
		super(name, email, password, "Admin");
	}
}