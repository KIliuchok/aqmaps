package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;

public class SensorData {
	private String location;
	private float battery;
	private String reading;
	private Boolean visited;

	public Boolean getVisited() {
		return visited;
	}

	public void setVisited(Boolean visited) {
		this.visited = visited;
	}

	public String getLocation() {
		return location;
	}

	public float getBattery() {
		return battery;
	}

	public String getReading() {
		return reading;
	}

	private double lat;
	private double lng;

	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public Coordinates getCoordinates() {
		var coordinates = new Coordinates(lng, lat);
		return coordinates;
	}

	public SensorData (Point point) {
		this.lat = point.latitude();
		this.lng = point.longitude();
	}
}
