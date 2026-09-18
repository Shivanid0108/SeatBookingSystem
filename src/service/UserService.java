package service;

import java.util.Collection;
import java.util.HashMap;

import module.Admin;
import module.BusinessOwner;
import module.ClientUser;
import module.User;

/**
 * WHAT: Manages all user-related operations — registration, login, and lookup.
 *
 * WHY EXISTS SEPARATELY FROM USER? User.java describes what a user IS
 * (data/fields). UserService describes what you can DO with users
 * (behaviour/logic). This is the Single Responsibility Principle — each class
 * has one job.
 *
 * WHY HASHMAP instead of List? We always search users by EMAIL (login,
 * duplicate check, admin lookup). HashMap<email, User> → O(1) lookup vs O(n)
 * linear search through a List. With 10,000 users: HashMap finds in 1 step,
 * List searches up to 10,000 steps.
 *
 * SPRING BOOT MIGRATION: HashMap<String, User> users → UserRepository (JPA)
 * users.put(email, user) → userRepository.save(user) users.get(email) →
 * userRepository.findByEmail(email) users.containsKey(email) →
 * userRepository.existsByEmail(email) users.remove(email) →
 * userRepository.deleteByEmail(email) Business logic (validation) stays
 * identical — only storage calls change.
 */
public class UserService {

	/**
	 * WHY email as HashMap key? Email is unique per user — perfect key for a
	 * HashMap. Every operation that needs a user starts with their email: - Login:
	 * find user by email, check password - Registration: check if email already
	 * exists - Admin lookup: find any user by email
	 *
	 * WHY List<User> would be worse: Finding by email = loop through all users →
	 * O(n) HashMap lookup = direct access → O(1)
	 */
	private HashMap<String, User> users = new HashMap<>();

	// ── Validation helpers ─────────────────────────────────────────────────

	/**
	 * WHY private validate() helper? All three register methods (Admin,
	 * BusinessOwner, ClientUser) need the same validation — DRY principle. One
	 * place to update if rules change.
	 *
	 * Returns null if valid, error message String if invalid. Caller checks: if
	 * (err != null) return err;
	 */
	private String validate(String name, String email, String password) {
		if (!isValidName(name))
			return "Invalid name";
		if (!isValidEmail(email))
			return "Invalid email (use gmail/yahoo/outlook)";
		if (users.containsKey(email))
			return "Email already registered";
		if (!isValidPassword(password))
			return "Password must be 8+ chars, start with letter, contain upper+lower+digit+#/&/@";
		return null; // null = valid
	}

	private boolean isValidName(String name) {
		// null check first — calling .trim() on null throws NullPointerException
		return name != null && !name.trim().isEmpty();
	}

	private boolean isValidEmail(String email) {
		if (email == null || !email.contains("@"))
			return false;
		// Only allow common email providers — prevents test/fake emails
		return email.endsWith("@gmail.com") || email.endsWith("@yahoo.com") || email.endsWith("@outlook.com");
	}

	private boolean isValidPassword(String password) {
		// Length check first — avoids index out of bounds on charAt(0)
		if (password == null || password.length() < 8)
			return false;

		// WHY no digit at start? Passwords starting with digits are weak.
		if (Character.isDigit(password.charAt(0)))
			return false;

		boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
		for (char c : password.toCharArray()) {
			if (Character.isUpperCase(c))
				hasUpper = true;
			else if (Character.isLowerCase(c))
				hasLower = true;
			else if (Character.isDigit(c))
				hasDigit = true;
			else if (c == '#' || c == '&' || c == '@')
				hasSpecial = true;
		}
		// ALL four must be present — no short-circuit allowed
		return hasUpper && hasLower && hasDigit && hasSpecial;
	}

	// ── Registration ───────────────────────────────────────────────────────

	/**
	 * WHY three separate register methods instead of one registerUser(role)? Type
	 * safety — each method creates the correct subclass. registerAdmin() creates
	 * Admin, not a generic User with role="Admin". The subclass constructor
	 * hardcodes the role — no way to pass wrong role.
	 *
	 * PATTERN: validate → create → store → return message Same pattern as a real
	 * REST API: validate input → create entity → save → respond.
	 */
	public String registerAdmin(String name, String email, String password) {
		String err = validate(name, email, password);
		if (err != null)
			return err;
		users.put(email, new Admin(name, email, password));
		return "Registered successfully";
	}

	public String registerBusinessOwner(String name, String email, String password) {
		String err = validate(name, email, password);
		if (err != null)
			return err;
		users.put(email, new BusinessOwner(name, email, password));
		return "Registered successfully";
	}

	public String registerClientUser(String name, String email, String password) {
		String err = validate(name, email, password);
		if (err != null)
			return err;
		users.put(email, new ClientUser(name, email, password));
		return "Registered successfully";
	}

	// ── Login ──────────────────────────────────────────────────────────────

	/**
	 * WHY return User object instead of String? Caller (Main.java) needs the actual
	 * User object to: - Display "Welcome [name]" - Cast to correct subtype for
	 * role-specific menu - Pass to service methods that need the logged-in user
	 *
	 * WHY return null on failure instead of throwing exception? null is the
	 * standard Java convention for "not found". Caller checks: if (user == null) →
	 * show error, else → show menu.
	 *
	 * WHY GENERIC error message? SECURITY: "Invalid email or password" (not "Email
	 * not found" or "Wrong password") Specific messages help hackers — they reveal
	 * which part is wrong. Generic message: attacker can't tell if email exists or
	 * password is wrong.
	 *
	 * WHY check role? Prevents a ClientUser from logging in as Admin with same
	 * credentials. Role is stored in the User object at registration — we verify it
	 * matches.
	 */
	public User loginUser(String email, String password, String role) {
		User user = users.get(email); // O(1) HashMap lookup
		if (user == null)
			return null; // email not found
		if (!user.getPassword().equals(password))
			return null; // wrong password
		if (!user.getRole().equals(role))
			return null; // wrong role
		return user; // all checks passed
	}

	// ── Lookup & Management ────────────────────────────────────────────────

	/**
	 * WHY public? (was private before) Admin needs to find any user by email for
	 * management operations. ClientUser accesses their own data via
	 * client.getBookings() directly — no search needed for personal data.
	 *
	 * SPRING BOOT: becomes userRepository.findByEmail(email)
	 */
	public User findUserByEmail(String email) {
		return users.get(email); // O(1) — HashMap direct lookup
	}

	public boolean emailExists(String email) {
		return users.containsKey(email); // O(1) — no loop needed
	}

	/**
	 * WHY return Collection<User> not List<User>? users.values() returns
	 * Collection<User> — the natural return type of HashMap values. Converting to
	 * List would create an unnecessary copy. Caller just needs to iterate —
	 * Collection supports that.
	 *
	 * SPRING BOOT: becomes userRepository.findAll()
	 */
	public Collection<User> getUsers() {
		return users.values();
	}

	public String deleteUser(String email) {
		if (!users.containsKey(email))
			return "User not found";
		users.remove(email);
		return "User deleted successfully";
	}
}