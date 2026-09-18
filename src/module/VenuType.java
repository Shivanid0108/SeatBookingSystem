package module;

/**
 * WHAT: Enum defining all valid venue types a BusinessOwner can register.
 *
 * WHY ENUM instead of String? Prevents invalid venue types being entered.
 * "Cinmea" (typo) would compile as String but not as enum.
 *
 * WHY THESE TYPES? From assignment: "cinema, restaurant, hotel conference room,
 * etc." We've added SPORTS_ARENA to support larger venues with many rows.
 *
 * NOTE ON TYPO: Class is named VenuType (missing 'e') — intentional decision to
 * keep consistency with existing codebase. Would be VenueType in production.
 *
 * SPRING BOOT NOTE:
 * 
 * @Enumerated(EnumType.STRING) on Venue.type field.
 */
public enum VenuType {
	CINEMA, RESTAURANT, CONFERENCE_ROOM, THEATRE, HOTEL, SPORTS_ARENA
}