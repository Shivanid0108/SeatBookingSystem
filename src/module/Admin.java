package module;

/**
 * Admin extends User — Admin IS-A User.
 * 
 * WHY so simple? Only a constructor? Because Admin shares all fields with User
 * (name, email, password, role). Admin-specific behaviour (monitorAccess,
 * handleComplaint) will live in AdminService, not in this model class.
 * 
 * SEPARATION OF CONCERNS: - Admin.java (model) = what an Admin IS (data/fields)
 * - AdminService.java (service) = what an Admin can DO (behaviour/actions)
 * 
 * SPRING BOOT NOTE: This will get @Entity and @DiscriminatorValue("Admin")
 * annotations to map to the correct DB table row.
 */
public class Admin extends User {

	/**
	 * WHY does Admin not take "role" as a parameter? Because Admin ALWAYS has role
	 * "Admin" — no exceptions. Hardcoding it here means: 1. The caller can't
	 * accidentally pass the wrong role 2. No extra parameter needed — simpler API
	 * 3. The role is guaranteed to always be correct
	 * 
	 * External caller: new Admin("John", "john@gmail.com", "Pass@123") Internally:
	 * super(name, email, password, "Admin") → role set automatically
	 */
	public Admin(String name, String email, String password) {
		super(name, email, password, "Admin");
	}
}