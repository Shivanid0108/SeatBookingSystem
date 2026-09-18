package ui;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import module.Booking;
import module.BusinessOwner;
import module.ClientUser;
import module.ResellListing;
import module.Room;
import module.Seat;
import module.Show;
import module.User;
import module.VenuType;
import module.Venue;
import service.AdminService;
import service.BookingService;
import service.ResellService;
import service.UserService;
import service.VenueService;

public class Main {

	private static final UserService userService = new UserService();
	private static final VenueService venueService = new VenueService();
	private static final BookingService bookingService = new BookingService();
	private static final ResellService resellService = new ResellService();
	private static final AdminService adminService = new AdminService(userService, venueService);
	private static final Scanner sc = new Scanner(System.in);

	// =========================================================================
	// ENTRY POINT
	// =========================================================================
	public static void main(String[] args) {
		banner("SEAT BOOKING SYSTEM");
		while (true) {
			System.out.println("\n  1. Register");
			System.out.println("  2. Login");
			System.out.println("  3. Exit");
			switch (readInt("Choice")) {
			case 1 -> handleRegistration();
			case 2 -> handleLogin();
			case 3 -> {
				System.out.println("\nGoodbye!");
				sc.close();
				return;
			}
			default -> err("Invalid choice.");
			}
		}
	}

	// =========================================================================
	// REGISTRATION
	// =========================================================================
	private static void handleRegistration() {
		banner("REGISTER");
		while (true) {
			System.out.println("  1. Admin");
			System.out.println("  2. Business Owner");
			System.out.println("  3. Client");
			System.out.println("  0. Back");
			int choice = readInt("Role");
			if (choice == 0)
				return;
			if (choice < 1 || choice > 3) {
				err("Invalid choice.");
				continue;
			}

			String name = prompt("Name");
			String email = prompt("Email");
			String password = promptPassword();

			String result = switch (choice) {
			case 1 -> userService.registerAdmin(name, email, password);
			case 2 -> userService.registerBusinessOwner(name, email, password);
			default -> userService.registerClientUser(name, email, password);
			};

			System.out.println("\n  >> " + result);
			if (result.equals("Registered successfully"))
				return;
		}
	}

	// =========================================================================
	// LOGIN
	// =========================================================================
	private static void handleLogin() {
		banner("LOGIN");
		while (true) {
			String email = prompt("Email");
			String password = promptPassword();

			User user = userService.loginUser(email, password, "Admin");
			if (user == null)
				user = userService.loginUser(email, password, "BusinessOwner");
			if (user == null)
				user = userService.loginUser(email, password, "ClientUser");

			if (user == null) {
				err("Invalid email or password.");
				System.out.println("  Try again? (1=Yes  0=Back)");
				if (readInt("") == 0)
					return;
				continue;
			}

			System.out.println("\n  Welcome back, " + user.getName() + "! [" + user.getRole() + "]");
			switch (user.getRole()) {
			case "Admin" -> adminMenu(user);
			case "BusinessOwner" -> businessOwnerMenu((BusinessOwner) user);
			default -> clientMenu((ClientUser) user);
			}
			return;
		}
	}

	// =========================================================================
	// ADMIN MENU
	// =========================================================================
	private static void adminMenu(User admin) {
		while (true) {
			banner("ADMIN — " + admin.getName());
			System.out.println("  1. View all users");
			System.out.println("  2. Find user by email");
			System.out.println("  3. Delete user");
			System.out.println("  4. Suspend user");
			System.out.println("  5. Reinstate user");
			System.out.println("  6. View all venues");
			System.out.println("  0. Logout");

			switch (readInt("Choice")) {
			case 1 -> {
				banner("ALL USERS");
				var users = adminService.getAllUsers();
				if (users.isEmpty()) {
					info("No users registered.");
					break;
				}
				int i = 1;
				for (User u : users)
					System.out.printf("  %d. %-20s %-30s [%s]%n", i++, u.getName(), u.getEmail(), u.getRole());
			}
			case 2 -> {
				User found = adminService.findUser(prompt("Email to search"));
				if (found == null)
					err("User not found.");
				else
					System.out.printf("%n  Found: %s | %s | %s%n", found.getName(), found.getEmail(), found.getRole());
			}
			case 3 -> System.out.println("\n  >> " + adminService.deleteUser(prompt("Email to delete")));
			case 4 -> System.out.println("\n  >> " + adminService.suspendUser(prompt("Email to suspend")));
			case 5 -> {
				String email = prompt("Email to reinstate");
				String role = prompt("Restore as (Admin/BusinessOwner/ClientUser)");
				System.out.println("\n  >> " + adminService.reinstateUser(email, role));
			}
			case 6 -> printVenues(adminService.getAllVenues());
			case 0 -> {
				info("Logged out.");
				return;
			}
			default -> err("Invalid choice.");
			}
		}
	}

	// =========================================================================
	// BUSINESS OWNER MENU
	// =========================================================================
	private static void businessOwnerMenu(BusinessOwner owner) {
		while (true) {
			banner("BUSINESS OWNER — " + owner.getName());
			System.out.println("  1. Create venue");
			System.out.println("  2. Add room to venue");
			System.out.println("  3. Add show to room"); // NEW
			System.out.println("  4. View my venues & shows"); // updated
			System.out.println("  5. Cancel a show"); // NEW
			System.out.println("  6. Update venue price");
			System.out.println("  7. Delete venue");
			System.out.println("  0. Logout");

			switch (readInt("Choice")) {
			case 1 -> {
				String name = prompt("Venue name");
				VenuType[] types = VenuType.values();
				System.out.println("  Venue types:");
				for (int i = 0; i < types.length; i++)
					System.out.println("    " + (i + 1) + ". " + types[i]);
				int t = readInt("Type");
				if (t < 1 || t > types.length) {
					err("Invalid type.");
					break;
				}
				BigDecimal price = readBigDecimal("Regular seat price ($)");
				System.out.println("\n  >> " + venueService.createVenue(owner, name, types[t - 1], price));
			}
			case 2 -> {
				Venue v = pickVenue();
				if (v == null)
					break;
				int rows = readInt("Number of rows");
				int cols = readInt("Number of columns (max 26)");
				System.out.println("\n  >> " + venueService.addRoom(v, rows, cols));
			}
			case 3 -> {
				// BusinessOwner creates a show — sets the date and time
				Venue v = pickVenue();
				if (v == null)
					break;
				Room r = pickRoom(v);
				if (r == null)
					break;
				String showName = prompt("Show name (e.g. Avengers - 7pm)");
				LocalDateTime dt = readDateTime(); // only BusinessOwner enters date/time!
				System.out.println("\n  >> " + venueService.createShow(r, showName, dt));
			}
			case 4 -> {
				// Show venues with their rooms and shows
				List<Venue> venues = venueService.getVenues();
				if (venues.isEmpty()) {
					info("No venues yet.");
					break;
				}
				for (Venue v : venues) {
					System.out.println("\n  ── " + v.getName() + " | " + v.getType() + " | $" + v.getRegularPrice());
					List<Room> rooms = v.getRooms();
					if (rooms.isEmpty()) {
						System.out.println("    No rooms yet.");
						continue;
					}
					for (int ri = 0; ri < rooms.size(); ri++) {
						Room r = rooms.get(ri);
						System.out.printf("    Room %d: %d×%d seats%n", ri + 1, r.getTotalRows(), r.getTotalCols());
						List<Show> shows = r.getAllShows();
						if (shows.isEmpty()) {
							System.out.println("      No shows scheduled.");
							continue;
						}
						for (int si = 0; si < shows.size(); si++)
							System.out.printf("      Show %d: %s%n", si + 1, shows.get(si));
					}
				}
			}
			case 5 -> {
				Venue v = pickVenue();
				if (v == null)
					break;
				Room r = pickRoom(v);
				if (r == null)
					break;
				Show show = pickShow(r, true);
				if (show == null)
					break; // all shows including cancelled
				System.out.println("\n  >> " + venueService.cancelShow(show));
			}
			case 6 -> {
				Venue v = pickVenue();
				if (v == null)
					break;
				BigDecimal price = readBigDecimal("New regular price ($)");
				System.out.println("\n  >> " + venueService.updatePrice(v, price));
			}
			case 7 -> {
				Venue v = pickVenue();
				if (v == null)
					break;
				System.out.print("  Confirm delete '" + v.getName() + "'? (y/n): ");
				if (sc.nextLine().trim().equalsIgnoreCase("y"))
					System.out.println("\n  >> " + venueService.deleteVenue(v));
			}
			case 0 -> {
				info("Logged out.");
				return;
			}
			default -> err("Invalid choice.");
			}
		}
	}

	// =========================================================================
	// CLIENT MENU
	// =========================================================================
	private static void clientMenu(ClientUser client) {
		while (true) {
			banner("CLIENT — " + client.getName());
			System.out.println("  1. Browse venues & shows");
			System.out.println("  2. Book a seat");
			System.out.println("  3. My bookings");
			System.out.println("  4. Cancel a booking");
			System.out.println("  5. Resell a booking");
			System.out.println("  6. Browse resell market");
			System.out.println("  7. Buy from resell market");
			System.out.println("  0. Logout");

			switch (readInt("Choice")) {
			case 1 -> {
				// Client browses venues and sees available shows
				List<Venue> venues = venueService.getVenues();
				if (venues.isEmpty()) {
					info("No venues available.");
					break;
				}
				for (Venue v : venues) {
					System.out.println("\n  ── " + v.getName() + " | " + v.getType());
					for (int ri = 0; ri < v.getRooms().size(); ri++) {
						Room r = v.getRooms().get(ri);
						List<Show> active = r.getActiveShows();
						System.out.printf("    Room %d (%d×%d seats):%n", ri + 1, r.getTotalRows(), r.getTotalCols());
						if (active.isEmpty())
							System.out.println("      No shows available.");
						else
							for (int si = 0; si < active.size(); si++)
								System.out.printf("      %d. %s%n", si + 1, active.get(si));
					}
				}
			}
			case 2 -> {
				// Client books a seat — picks a show, no date entry!
				Venue v = pickVenue();
				if (v == null)
					break;
				Room r = pickRoom(v);
				if (r == null)
					break;
				Show show = pickShow(r, false);
				if (show == null)
					break; // active shows only
				r.displaySeatMap(show.getShowDateTime());
				String label = prompt("Enter seat (e.g. 3C)").toUpperCase();
				Seat seat = findSeat(r, label);
				if (seat == null) {
					err("Seat '" + label + "' not found.");
					break;
				}
				if (!seat.isAvailableFor(show.getShowDateTime())) {
					err("Seat already booked for this show.");
					break;
				}
				System.out.printf("  Seat: %s | Zone %d | Price: $%s%n", seat.getSeatLabel(), seat.getZone(),
						seat.getCalculatedPrice());
				System.out.print("  Confirm booking? (y/n): ");
				if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
					info("Booking cancelled.");
					break;
				}
				System.out.println("\n  >> " + bookingService.reserveSeat(client, seat, show));
			}
			case 3 -> {
				List<Booking> list = bookingService.getBookingbyUser(client);
				if (list.isEmpty()) {
					info("No bookings found.");
					break;
				}
				banner("MY BOOKINGS");
				for (int i = 0; i < list.size(); i++)
					System.out.printf("  %d. %s%n", i + 1, list.get(i));
			}
			case 4 -> {
				List<Booking> list = bookingService.getBookingbyUser(client);
				if (list.isEmpty()) {
					info("No bookings to cancel.");
					break;
				}
				for (int i = 0; i < list.size(); i++)
					System.out.printf("  %d. %s%n", i + 1, list.get(i));
				int idx = readInt("Select booking") - 1;
				if (idx < 0 || idx >= list.size()) {
					err("Invalid selection.");
					break;
				}
				System.out.println("\n  >> " + bookingService.cancelBooking(list.get(idx)));
			}
			case 5 -> {
				List<Booking> list = bookingService.getBookingbyUser(client);
				if (list.isEmpty()) {
					info("No bookings to resell.");
					break;
				}
				for (int i = 0; i < list.size(); i++)
					System.out.printf("  %d. %s%n", i + 1, list.get(i));
				int idx = readInt("Select booking to resell") - 1;
				if (idx < 0 || idx >= list.size()) {
					err("Invalid selection.");
					break;
				}
				BigDecimal price = readBigDecimal("Resell price ($)");
				BigDecimal discount = readBigDecimal("Discount ratio (0.00-1.00)");
				System.out.println("\n  >> " + resellService.resellSeat(client, list.get(idx), price, discount));
			}
			case 6 -> {
				List<ResellListing> all = resellService.getAllListings();
				if (all.isEmpty()) {
					info("No resell listings available.");
					break;
				}
				banner("RESELL MARKET");
				for (int i = 0; i < all.size(); i++)
					System.out.printf("  %d. %s%n", i + 1, all.get(i));
			}
			case 7 -> {
				List<ResellListing> all = resellService.getAllListings();
				if (all.isEmpty()) {
					info("No resell listings available.");
					break;
				}
				banner("RESELL MARKET");
				for (int i = 0; i < all.size(); i++)
					System.out.printf("  %d. %s%n", i + 1, all.get(i));
				int idx = readInt("Select listing to buy") - 1;
				if (idx < 0 || idx >= all.size()) {
					err("Invalid selection.");
					break;
				}
				ResellListing listing = all.get(idx);
				System.out.printf("  Price: $%s  Discount: %s%%%n", listing.getResellPrice(),
						listing.getDiscountRatio());
				System.out.print("  Confirm purchase? (y/n): ");
				if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
					info("Purchase cancelled.");
					break;
				}
				System.out.println("\n  >> " + resellService.buySeat(client, listing));
			}
			case 0 -> {
				info("Logged out.");
				return;
			}
			default -> err("Invalid choice.");
			}
		}
	}

	// =========================================================================
	// HELPERS
	// =========================================================================

	private static int readInt(String label) {
		while (true) {
			if (!label.isEmpty())
				System.out.print("  " + label + ": ");
			try {
				return Integer.parseInt(sc.nextLine().trim());
			} catch (NumberFormatException e) {
				err("Please enter a number.");
			}
		}
	}

	private static BigDecimal readBigDecimal(String label) {
		while (true) {
			System.out.print("  " + label + ": ");
			try {
				return new BigDecimal(sc.nextLine().trim());
			} catch (NumberFormatException e) {
				err("Please enter a valid number.");
			}
		}
	}

	private static String prompt(String label) {
		System.out.print("  " + label + ": ");
		return sc.nextLine().trim();
	}

	private static String promptPassword() {
		System.out.print("  Password (8+ chars, upper+lower+digit+#/&/@): ");
		return sc.nextLine().trim();
	}

	/**
	 * Only BusinessOwner calls this — client never enters date/time.
	 */
	private static LocalDateTime readDateTime() {
		final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		while (true) {
			System.out.print("  Show date & time (dd-MM-yyyy HH:mm): ");
			try {
				return LocalDateTime.parse(sc.nextLine().trim(), fmt);
			} catch (Exception e) {
				err("Invalid format — use dd-MM-yyyy HH:mm  e.g. 20-03-2026 19:00");
			}
		}
	}

	private static Venue pickVenue() {
		List<Venue> list = venueService.getVenues();
		if (list.isEmpty()) {
			info("No venues available yet.");
			return null;
		}
		printVenues(list);
		int idx = readInt("Select venue") - 1;
		if (idx < 0 || idx >= list.size()) {
			err("Invalid selection.");
			return null;
		}
		return list.get(idx);
	}

	private static Room pickRoom(Venue venue) {
		List<Room> rooms = venue.getRooms();
		if (rooms.isEmpty()) {
			info("No rooms in this venue yet.");
			return null;
		}
		System.out.println("\n  Rooms in " + venue.getName() + ":");
		for (int i = 0; i < rooms.size(); i++) {
			Room r = rooms.get(i);
			System.out.printf("    %d. %d rows x %d cols = %d seats%n", i + 1, r.getTotalRows(), r.getTotalCols(),
					r.getTotalRows() * r.getTotalCols());
		}
		int idx = readInt("Select room") - 1;
		if (idx < 0 || idx >= rooms.size()) {
			err("Invalid selection.");
			return null;
		}
		return rooms.get(idx);
	}

	/**
	 * Client picks from shows — no date entry.
	 * 
	 * @param showAll true = include cancelled (for BusinessOwner), false = active
	 *                only (for Client)
	 */
	private static Show pickShow(Room room, boolean showAll) {
		List<Show> shows = showAll ? room.getAllShows() : room.getActiveShows();
		if (shows.isEmpty()) {
			info(showAll ? "No shows in this room." : "No active shows available in this room.");
			return null;
		}
		System.out.println("\n  Available shows:");
		for (int i = 0; i < shows.size(); i++)
			System.out.printf("    %d. %s%n", i + 1, shows.get(i));
		int idx = readInt("Select show") - 1;
		if (idx < 0 || idx >= shows.size()) {
			err("Invalid selection.");
			return null;
		}
		return shows.get(idx);
	}

	private static void printVenues(List<Venue> list) {
		if (list.isEmpty()) {
			info("No venues found.");
			return;
		}
		System.out.println("\n  ── Venues ──────────────────────────────────");
		for (int i = 0; i < list.size(); i++) {
			Venue v = list.get(i);
			System.out.printf("  %d. %-20s | %-15s | $%s/seat | %d room(s)%n", i + 1, v.getName(), v.getType(),
					v.getRegularPrice(), v.getRooms().size());
		}
		System.out.println("  ────────────────────────────────────────────");
	}

	private static Seat findSeat(Room room, String label) {
		for (Seat[] row : room.getSeats())
			for (Seat s : row)
				if (s.getSeatLabel().equalsIgnoreCase(label))
					return s;
		return null;
	}

	private static void banner(String title) {
		System.out.println("\n╔══════════════════════════════╗");
		System.out.printf("║  %-28s║%n", title);
		System.out.println("╚══════════════════════════════╝");
	}

	private static void info(String msg) {
		System.out.println("\n  ℹ  " + msg);
	}

	private static void err(String msg) {
		System.out.println("\n  ✗  " + msg);
	}
}