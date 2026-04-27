package module;

import java.math.BigDecimal;

public class Seat {
	private String seatID;
	private int row;
	private char column;
	private BigDecimal calculatedPrice;
	private boolean isAvailable;
	private int zone;
	boolean isMiddleCol;

	public Seat(int row, int colIndex, int roomSize, BigDecimal regularPrice) {
		this.row = row;
		this.column = (char) ('A' + colIndex - 1);
		this.seatID = row + "" + column;
		this.isAvailable = true;

		if (roomSize % 2 == 0) {
			// even: two perfect middle columns
			isMiddleCol = (colIndex == roomSize / 2 || colIndex == roomSize / 2 + 1);
		} else {
			// odd: take the center and one to the right
			isMiddleCol = (colIndex == roomSize / 2 + 1 || colIndex == roomSize / 2 + 2);
		}

		if (row == 1) {
			this.zone = 1;
			calculatedPrice = regularPrice.multiply(new BigDecimal("2"));
		} else if (row == roomSize) {
			this.zone = 2;
			calculatedPrice = regularPrice.multiply(new BigDecimal("0.75"));
		} else if (isMiddleCol) {
			this.zone = 3;
			calculatedPrice = regularPrice.multiply(new BigDecimal("1.25"));
		} else {
			this.zone = 4;
			calculatedPrice = regularPrice;
		}

	}

	public int getZone() {
		return zone;
	}

	public String getSeatID() {
		return seatID;
	}

	public int getRow() {
		return row;
	}

	public char getColumn() {
		return column;
	}

	public BigDecimal getCalculatedPrice() {
		return calculatedPrice;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

}
