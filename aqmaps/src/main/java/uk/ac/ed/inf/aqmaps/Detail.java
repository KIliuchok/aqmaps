package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class Detail {
	public int direction;
	public Point estimatedPoint;
	public Point startingPoint;
	
	public Detail (int direction, Point point, Point da) {
		this.direction = direction;
		this.estimatedPoint = point;
		this.startingPoint = da;
	}

}
