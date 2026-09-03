package module;

/**
 * User is ABSTRACT because we never want to create a plain "User" object. In
 * the real world, every person is either an Admin, BusinessOwner, or
 * ClientUser. Making it abstract forces this rule at the compiler level.
 * 
 * WHY ABSTRACT? Think of "Animal" — you never just create an "Animal", you
 * create a Dog or Cat. Same here — you never just create a "User", you create
 * an Admin or ClientUser.
 * 
 * SPRING BOOT NOTE: When we migrate to Spring Boot, this class will get:
 * - @Entity annotation (maps to DB table) - @Inheritance(strategy =
 * InheritanceType.JOINED) (each subclass gets its own table) - @Id
 * and @GeneratedValue on the id field
 */
public abstract class User {

	/**
	 * WHY Long instead of UUID String?
	 * 
	 * We switched from UUID (String) to Long for these reasons: 1. Spring Boot JPA
	 * uses Long with @GeneratedValue by default 2. Database auto-increment works
	 * naturally with Long 3. Faster DB lookups — comparing numbers is faster than
	 * comparing strings 4. Less storage space in DB — Long (8 bytes) vs UUID String
	 * (36 bytes)
	 * 
	 * In Spring Boot this will become:
	 * 
	 * @Id
	 * @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	 */
	private Long id;

	/**
	 * WHY store name, email, password, role in the PARENT class? Because ALL users
	 * (Admin, BusinessOwner, ClientUser) share these fields. This is the DRY
	 * principle — Don't Repeat Yourself. Instead of writing these 4 fields in every
	 * subclass, we write them once here.
	 */
	private String name;
	private String email;
	private String password;

	/**
	 * WHY store role as a String field instead of just relying on instanceof?
	 * 
	 * 1. Database — when we save to DB, role becomes a column ("Admin",
	 * "BusinessOwner", "ClientUser") 2. Login — we compare role strings to verify
	 * the user is logging in as the right type 3. Future REST API — role will be
	 * used for authorization (who can access what endpoint)
	 * 
	 * Each subclass sets this automatically in its constructor — the caller never
	 * passes it in. This prevents someone from creating an Admin object with role =
	 * "ClientUser".
	 */
	private String role;

	/**
	 * WHY does the constructor take role as a parameter if callers don't pass it?
	 * 
	 * Because the SUBCLASSES pass it — not the external caller. Admin calls:
	 * super(name, email, password, "Admin") The external caller just does: new
	 * Admin(name, email, password)
	 * 
	 * The role is hardcoded in each subclass constructor — clean and safe.
	 * 
	 * WHY no id in constructor? Because in console app, id is null (we don't use it
	 * yet). In Spring Boot, JPA will auto-generate it — we never set it manually.
	 */
	public User(String name, String email, String password, String role) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
		// id is intentionally not set here
		// console app: not needed yet
		// Spring Boot: JPA will auto-generate it
	}

	/**
	 * WHY only getter for id, no setter? IDs should never change after creation.
	 * Once a user is created with ID 5, it stays ID 5 forever. No setter prevents
	 * accidental ID changes.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * WHY getter but no setter for name? A user's name doesn't change in this
	 * system. If we need to allow name changes later, we add a setter then. Start
	 * restrictive, loosen later — not the other way around.
	 */
	public String getName() {
		return name;
	}

	/**
	 * WHY setter for email? Email CAN change — user might update their email
	 * address. So we provide both getter and setter.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	/**
	 * WHY setter for password? User might change their password (forgot password,
	 * update password features). In Spring Boot, password will be hashed before
	 * storing — never stored as plain text.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * WHY getter and setter for role? Getter — needed for login verification and
	 * future authorization. Setter — Admin might reassign roles in future (promote
	 * a ClientUser to BusinessOwner).
	 */
	public void setRole(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}
}