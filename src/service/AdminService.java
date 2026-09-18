package service;

import java.util.Collection;
import java.util.List;

import module.User;
import module.Venue;

/**
 * WHAT: Provides admin-specific operations by delegating to other services.
 *
 * WHY DOES ADMINSERVICE EXIST if UserService and VenueService already have the
 * data? This is the FACADE PATTERN — AdminService is a unified interface for
 * admin operations. Admin needs to access both users AND venues from one place.
 * Without AdminService: Main.java would need to know about both services for
 * Admin menu. With AdminService: Main.java calls adminService.X() for
 * everything admin-related.
 *
 * WHY DELEGATION instead of duplicating logic? AdminService doesn't manage its
 * own data — it DELEGATES to existing services. adminService.getAllUsers() →
 * userService.getUsers() adminService.getAllVenues() → venueService.getVenues()
 *
 * If UserService.getUsers() changes, AdminService automatically benefits. No
 * code duplication — follows DRY principle.
 *
 * WHAT CAN ADMIN DO? - View all users (any type) - Find a specific user by
 * email - Delete a user - Suspend a user (change role to "SUSPENDED") -
 * Reinstate a suspended user - View all venues in the system
 *
 * SPRING BOOT NOTE: AdminService stays almost identical — it already delegates
 * to repositories. Will add @Service annotation and inject UserRepository,
 * VenueRepository.
 */
public class AdminService {

	/**
	 * WHY constructor injection instead of creating services internally? Dependency
	 * injection — AdminService receives its dependencies from outside. This is the
	 * "Dependency Inversion Principle" (the D in SOLID). Benefits: - Same
	 * UserService instance shared across the app (no duplicate data) - Easier to
	 * test — can inject mock services - Cleaner than new UserService() inside
	 * AdminService
	 *
	 * SPRING BOOT: @Autowired constructor injection handles this automatically.
	 */
	private final UserService userService;
	private final VenueService venueService;

	public AdminService(UserService userService, VenueService venueService) {
		this.userService = userService;
		this.venueService = venueService;
	}

	// ── User management (delegates to UserService) ─────────────────────────

	/** Returns all users in the system — Admin-only operation */
	public Collection<User> getAllUsers() {
		return userService.getUsers();
	}

	/** Find any user by email — Admin needs to look up any user */
	public User findUser(String email) {
		return userService.findUserByEmail(email);
	}

	/** Permanently delete a user from the system */
	public String deleteUser(String email) {
		return userService.deleteUser(email);
	}

	/**
	 * WHY suspend instead of delete? Deletion removes all history — bad for audit
	 * trails. Suspension blocks the user (role = "SUSPENDED") while preserving
	 * their record. Admin can reinstate later if needed. SUSPENDED users fail login
	 * because their role doesn't match any valid type.
	 */
	public String suspendUser(String email) {
		User user = userService.findUserByEmail(email);
		if (user == null)
			return "User not found";
		user.setRole("SUSPENDED");
		return "User " + user.getName() + " suspended";
	}

	/**
	 * WHY pass originalRole as parameter? AdminService doesn't know what role the
	 * user had before suspension. Admin decides which role to restore to — could be
	 * a role change. Flexible: suspend a ClientUser, reinstate as BusinessOwner if
	 * needed.
	 */
	public String reinstateUser(String email, String role) {
		User user = userService.findUserByEmail(email);
		if (user == null)
			return "User not found";
		user.setRole(role);
		return "User " + user.getName() + " reinstated as " + role;
	}

	// ── Venue management (delegates to VenueService) ───────────────────────

	/** Returns all venues — Admin can see all venues in the system */
	public List<Venue> getAllVenues() {
		return venueService.getVenues();
	}
}