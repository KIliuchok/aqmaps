package uk.ac.ed.inf.aqmaps;

import java.math.BigDecimal;
import com.mapbox.geojson.*;

public class AirQualityEntry {
	String location;
	BigDecimal battery;
	String reading;	
	String lat;
	String lng;
	Boolean visited;
}
