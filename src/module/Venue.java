package module;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Venue represents a physical location registered by a BusinessOwner. Examples:
 * Cinema, Restaurant, Conference Room, Hotel.
 * 
 * WHY does Venue exist separately from Room? One Venue can have MANY Rooms
 * (halls). A cinema building (Venue) has multiple screening halls (Rooms). A
 * restaurant (Venue) might have multiple dining sections (Rooms).
 * 
 * RELATIONSHIP: BusinessOwner HAS-MANY Venues (one owner can register multiple
 * venues) Venue HAS-MANY Rooms (one venue can have multiple rooms/halls)
 * 
 * SPRING BOOT NOTE: This class will get: - @Entity - @ManyToOne on
 * businessOwner (many venues belong to one owner) - @OneToMany on rooms (one
 * venue has many rooms)
 */
public class Venue {

	/**
	 * WHY Long id instead of UUID String?
	 * 
	 * 1. JPA/Hibernate auto-generates Long IDs with @GeneratedValue 2. DB foreign
	 * keys work better with Long (faster joins) 3. Simpler than UUID for this use
	 * case
	 * 
	 * For console app: id stays null (not needed yet) For Spring
	 * Boot: @Id @GeneratedValue will handle it
	 */
	private Long id;

	/**
	 * WHY "name" and not "venueName"? Spring Boot JPA convention — field name maps
	 * directly to DB column name. "name" → column "name" in venues table
	 * "venueName" → column "venue_name" (extra word, redundant since table is
	 * already "venues")
	 * 
	 * Cleaner and follows standard naming conventions.
	 */
	private String name;

	/**
	 * WHY String type and not an enum? For now String is flexible — cinema,
	 * restaurant, hotel, etc. We can convert to enum later if we want to restrict
	 * values.
	 * 
	 * In Spring Boot: could become @Enumerated(EnumType.STRING) with a VenueType
	 * enum.
	 */
	private String type;

	/**
	 * WHY BigDecimal for price? Money must NEVER use double or float — floating
	 * point causes rounding errors. Example: 0.1 + 0.2 = 0.30000000000000004 in
	 * double! BigDecimal is exact — no rounding errors for financial calculations.
	 * 
	 * In Spring Boot DB: maps to DECIMAL(10,2) column.
	 */
	private BigDecimal regularPrice;

	/**
	 * WHY List<Room> here? Venue HAS-MANY Rooms — composition relationship. The
	 * diamond symbol in your UML diagram shows this.
	 * 
	 * Rooms are added AFTER venue creation (Option B we chose earlier) because
	 * BusinessOwner first registers the venue, then adds rooms separately.
	 * 
	 * In Spring Boot:
	 * 
	 * @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL) private List<Room>
	 *                     rooms;
	 */
	private List<Room> rooms = new ArrayList<>();

	/**
	 * WHY no id in constructor? Console app: id not needed yet Spring Boot: JPA
	 * auto-generates it — we never set it manually
	 * 
	 * WHY no rooms in constructor? Rooms are added separately after venue creation.
	 * Constructor only takes what's needed at creation time.
	 */
	public Venue(String name, String type, BigDecimal regularPrice) {
		this.name = name;
		this.type = type;
		this.regularPrice = regularPrice;
	}

	// Getter only — id never changes after DB assigns it
	public Long getId() {
		return id;
	}

	// Setter for name — venue name might change (rebranding)
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	// Setter for type — venue might change its business type
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	// Setter for price — BusinessOwner might update pricing
	public void setRegularPrice(BigDecimal regularPrice) {
		this.regularPrice = regularPrice;
	}

	public BigDecimal getRegularPrice() {
		return regularPrice;
	}

	/**
	 * WHY addRoom() method instead of just exposing the list? Controlled access —
	 * we decide HOW rooms are added. Later we can add: "maximum 10 rooms per venue"
	 * validation here. Without touching any other code.
	 */
	public void addRoom(Room room) {
		rooms.add(room);
	}

	public List<Room> getRooms() {
		return rooms;
	}
}