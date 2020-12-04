package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;

public class Sensor {
	private String location;
	private float battery;
	private String reading;
	private double latitude;
	private double longitude;

	public String getLocation() {
		return location;
	}

	public float getBattery() {
		return battery;
	}

	public String getReading() {
		return reading;
	}

	public void setLat(double lat) {
		this.latitude = lat;
	}

	public void setLng(double lng) {
		this.longitude = lng;
	}

	public Coordinates getCoordinates() {
		var coordinates = new Coordinates(longitude, latitude);
		return coordinates;
	}

	public Sensor(Point point) {
		this.latitude = point.latitude();
		this.longitude = point.longitude();
	}
}
