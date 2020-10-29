package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class Detail {
	public double distance;
	public Point estimatedPoint;
	
	public Detail (double dist, Point point) {
		this.distance = dist;
		this.estimatedPoint = point;
	}

}
