package uk.ac.ed.inf.aqmaps;

public class Coordinates {
	private double latitude;
	private double longitude;

	public Coordinates(double lng, double lat) {
		this.longitude = lng;
		this.latitude = lat;
	}

	@Override
	public String toString() {
		return String.valueOf(longitude) + "," + String.valueOf(latitude);
	}

	public double getLat() {
		return latitude;
	}

	public double getLng() {
		return longitude;
	}
	
		
}
