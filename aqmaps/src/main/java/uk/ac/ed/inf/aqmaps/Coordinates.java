package uk.ac.ed.inf.aqmaps;

public class Coordinates {
	private double longitude;
	private double latitude;

	public Coordinates(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public double getLat() {
		return latitude;
	}

	public double getLng() {
		return longitude;
	}

}
