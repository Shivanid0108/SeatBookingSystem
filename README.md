# Seat Booking System

A console-based seat booking application built in Java. Supports three user roles — Admin, Business Owner, and Client — each with their own set of operations. Business owners register venues, set up rooms, and schedule shows. Clients browse available shows, book seats, and can resell their bookings on an internal marketplace.

---

## What it does

**Business Owner**
- Register a venue (cinema, restaurant, conference room, theatre, hotel, sports arena)
- Add rooms with custom row and column counts (up to 200 rows × 26 columns)
- Schedule shows with specific date and time
- Cancel shows, update pricing, delete venues

**Client**
- Browse venues and available shows
- View seat map with zone-based pricing before booking
- Book a seat for a specific show
- Cancel a booking
- List a booking for resale with a custom price and discount
- Buy seats from the resell marketplace

**Admin**
- View, find, delete, suspend, or reinstate any user
- View all venues in the system

---

## How seats are priced

Each room is divided into 4 zones based on position:

| Zone | Position | Price |
|------|----------|-------|
| Z1 | First row | 2× regular price |
| Z2 | Last row | 0.75× regular price |
| Z3 | Two middle columns | 1.25× regular price |
| Z4 | Everything else | Regular price |

Zone 3 (middle columns) handles both odd and even room sizes correctly.

---

## Architecture

```
src/
├── module/          # Data models
│   ├── User.java (abstract)
│   ├── Admin.java
│   ├── BusinessOwner.java
│   ├── ClientUser.java
│   ├── Venue.java
│   ├── Room.java
│   ├── Seat.java
│   ├── Show.java
│   ├── Booking.java
│   ├── BookingStatus.java (enum)
│   ├── ResellListing.java
│   └── VenuType.java (enum)
│
├── service/         # Business logic
│   ├── UserService.java
│   ├── VenueService.java
│   ├── BookingService.java
│   ├── ResellService.java
│   └── AdminService.java
│
└── ui/
    └── Main.java    # Console menus and user interaction
```

Three-layer architecture — model, service, and UI are completely separate. None of the service classes print to console. None of the UI classes validate data. Each layer has one job.

---

## Key design decisions

**Why HashMap for users and bookings?**
Login and booking lookup always start with an email. HashMap gives O(1) retrieval vs O(n) linear search through a list. With thousands of users this matters.

**Why 2D array for the seat grid?**
`seatGrid[row][col]` gives direct access to any seat in O(1). Also sets up naturally for the sliding window algorithm to find adjacent seats (planned feature).

**Why Show instead of letting clients enter date/time?**
Business owners control the schedule. Clients pick from what's available. Prevents bookings for arbitrary or past times.

**Why ResellService uses HashMap by zone?**
Clients browsing the resell market usually want seats by zone. HashMap<Integer, List<ResellListing>> gives O(1) zone-based retrieval.

**Why User is abstract?**
A plain User with no role makes no sense in this system. Abstract forces you to always create Admin, BusinessOwner, or ClientUser — never accidentally instantiate a base User.

---

## Running it

Requires Java 14+ (uses switch expressions).

```bash
git clone https://github.com/Shivanid0108/SeatBookingSystem.git
cd SeatBookingSystem
```

Compile and run `src/ui/Main.java` from your IDE (Eclipse, IntelliJ) or from the command line:

```bash
javac -d out src/**/*.java
java -cp out ui.Main
```

---

## What's next

This is the console version. The plan is to migrate to:
- **Spring Boot** — REST API with role-based endpoints
- **MySQL** — replace in-memory HashMaps with JPA repositories
- **React** — frontend for venue browsing and seat selection

---

## Built with

Java · OOP · HashMap · 2D Arrays · BigDecimal · LocalDateTime · Enums · 3-layer architecture