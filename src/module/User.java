package module;

/**
 * WHAT: Abstract base class for all user types in the system.
 *
 * WHY ABSTRACT? A plain "User" has no role and cannot do anything meaningful.
 * Every real person in this system is either an Admin, BusinessOwner, or
 * ClientUser. Making User abstract enforces this at compile time — you can
 * never accidentally create a plain User object.
 *
 * ANALOGY: Like "Animal" — you never create a plain Animal, always a Dog or
 * Cat.
 *
 * RELATIONSHIPS: - Admin extends User (IS-A) - BusinessOwner extends User
 * (IS-A) - ClientUser extends User (IS-A) - UserService manages a HashMap of
 * User objects
 *
 * SPRING BOOT NOTE: This will get @Entity and @Inheritance(strategy =
 * InheritanceType.JOINED) so each subclass maps to its own DB table joined on
 * id.
 */
public abstract class User {

	/**
	 * WHY Long instead of UUID String? - Spring Boot JPA uses Long
	 * with @GeneratedValue by default - DB auto-increment works naturally with Long
	 * - Faster DB lookups — comparing numbers is faster than comparing strings -
	 * Less storage: Long (8 bytes) vs UUID String (36 bytes)
	 *
	 * SPRING BOOT: @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	 */
	private Long id;

	/**
	 * WHY these four fields in the PARENT class? ALL user types share name, email,
	 * password, role. Declaring them once here follows the DRY principle. If we add
	 * phoneNumber later, we add it here once — all subclasses get it.
	 */
	private String name;
	private String email;
	private String password;

	/**
	 * WHY store role as a String field? 1. DB column — "Admin", "BusinessOwner",
	 * "ClientUser" stored directly 2. Login verification — compare role string to
	 * check correct login type 3. Future REST API authorization — role-based access
	 * control
	 *
	 * Each subclass sets this automatically in its own constructor. The external
	 * caller never passes role — it's always hardcoded per subclass. This prevents
	 * someone from creating Admin("name", "email", "pass", "ClientUser").
	 */
	private String role;

	/**
	 * WHY does constructor take role as parameter if callers don't pass it? The
	 * SUBCLASSES pass it — not the external caller. Admin calls: super(name, email,
	 * password, "Admin") External caller just does: new Admin(name, email,
	 * password)
	 *
	 * WHY no id in constructor? Console app: id not needed yet. Spring Boot: JPA
	 * auto-generates it — we never set it manually.
	 */
	public User(String name, String email, String password, String role) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
	}

	// Getter only — id set by DB, never changes after creation
	public Long getId() {
		return id;
	}

	// Getter only — name never changes in this system
	public String getName() {
		return name;
	}

	// Setter for email — user might update their email address
	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	// Setter for password — user might change their password
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	// Setter for role — Admin might reassign roles (e.g. suspend a user)
	public void setRole(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}
}