package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class Vector {
	private int direction;
	private Point targetPoint;
	private Point originPoint;

	public Vector(int direction, Point originPoint, Point targetPoint) {
		this.direction = direction;
		this.targetPoint = targetPoint;
		this.originPoint = originPoint;
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
