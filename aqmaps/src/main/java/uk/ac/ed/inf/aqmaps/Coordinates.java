package uk.ac.ed.inf.aqmaps;

public class Coordinates {
	double lat;
	double lng;

	public Coordinates(double lng, double lat) {
		this.lng = lng;
		this.lat = lat;
	}

	@Override
	public String toString() {
		return String.valueOf(lat) + "," + String.valueOf(lng);
	}
}
