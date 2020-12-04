package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class Vector {
	private int direction;
	private Point originPoint;
	private Point targetPoint;

	public Vector(int direction, Point originPoint, Point targetPoint) {
		this.direction = direction;
		this.originPoint = originPoint;
		this.targetPoint = targetPoint;
	}

	public int getDirection() {
		return direction;
	}

	public Point getTargetPoint() {
		return targetPoint;
	}

	public Point getOriginPoint() {
		return originPoint;
	}

}
