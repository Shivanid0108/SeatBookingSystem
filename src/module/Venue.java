package module;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Venue {

	private String venueID;
	private String venueName;
	private String venueType;
	private BigDecimal regularPrice;
	private List<Room> rooms = new ArrayList<>();

	public Venue(String venueName, String venueType, BigDecimal regularPrice) {
		this.venueID = UUID.randomUUID().toString();
		this.venueName = venueName;
		this.venueType = venueType;
		this.regularPrice = regularPrice;
	}

	public String getVenueID() {
		return venueID;
	}

	public void setVenueName(String venueName) {
		this.venueName = venueName;
	}

	public String getVenueName() {
		return venueName;
	}

	public void setVenueType(String venueType) {
		this.venueType = venueType;
	}

	public String getVenueType() {
		return venueType;
	}

	public void addRoom(Room room) {
		rooms.add(room);
	}

	public List<Room> getRooms() {
		return rooms;
	}

	public void setRegularPrice(BigDecimal regularPrice) {
		this.regularPrice = regularPrice;
	}

	public BigDecimal getRegularPrice() {
		return regularPrice;
	}

}
