package uk.ac.ed.inf.aqmaps;

import java.math.BigDecimal;
import com.mapbox.geojson.*;

public class SensorData {
	String location;
	BigDecimal battery;
	String reading;	
	String lat;
	String lng;
	Boolean visited;
	
	public SensorData (Point point) {
		this.lat = String.valueOf(point.latitude());
		this.lng = String.valueOf(point.longitude());
	}
}
